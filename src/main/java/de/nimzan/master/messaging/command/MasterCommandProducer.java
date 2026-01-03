package de.nimzan.master.messaging.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nimzan.master.config.MasterConfig;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MasterCommandProducer implements AutoCloseable {

    private final Connection connection;
    private final Session session;

    private final ConcurrentHashMap<String, MessageProducer> producersByQueue = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MasterCommandProducer() throws JMSException {
        String user = MasterConfig.activemqUser();
        String password = MasterConfig.activemqPassword();
        String url = MasterConfig.brokerUrl();
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(user, password, url);

        this.connection = cf.createConnection();
        this.connection.start();

        // one session for this producer instance
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void sendToQueue(String queueName, String type, Map<String, Object> args) throws JMSException {
        synchronized (this) {
            MessageProducer producer = producersByQueue.computeIfAbsent(queueName, q -> {
                try {
                    Queue queue = session.createQueue(q);
                    MessageProducer p = session.createProducer(queue);
                    p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    return p;
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                UUID commandUid = UUID.randomUUID();

                Map<Object, Object> envelope = Map.of(
                        "commandUid", commandUid,
                        "type", type,
                        "args", args == null ? Map.of() : args,
                        "meta", Map.of(
                                "timestamp", Instant.now().toEpochMilli(),
                                "source", "master"
                        )
                );

                String json = objectMapper.writeValueAsString(envelope);

                TextMessage msg = session.createTextMessage(json);
                msg.setStringProperty("content-type", "application/json");
                msg.setStringProperty("commandType", type);

                producer.send(msg);
            } catch (Exception ex) {
                JMSException jmse = new JMSException("Failed to serialize event to JSON: " + ex.getMessage());
                jmse.setLinkedException(ex);
                throw jmse;
            }
        }
    }

    @Override
    public void close() {
        for (MessageProducer p : producersByQueue.values()) {
            try { p.close(); } catch (Exception ignored) {}
        }
        try { session.close(); } catch (Exception ignored) {}
        try { connection.close(); } catch (Exception ignored) {}
    }
}