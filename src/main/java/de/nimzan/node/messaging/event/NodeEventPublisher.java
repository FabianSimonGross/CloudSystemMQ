package de.nimzan.node.messaging.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nimzan.node.config.NodeConfig;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NodeEventPublisher implements AutoCloseable {

    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // JMS Session ist NICHT thread-safe -> publish() synchronisieren, wenn mehrere Threads senden
    public NodeEventPublisher(String topicName) throws JMSException {
        String user = NodeConfig.activemqUser();
        String password = NodeConfig.activemqPassword();
        String url = NodeConfig.brokerUrl();

        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(user, password, url);

        this.connection = cf.createConnection();
        this.connection.start();

        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);

        this.producer = session.createProducer(topic);
        this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }

    public synchronized void publish(String type, Map<String, Object> fields) throws JMSException {
        try {
            LinkedHashMap<String, Object> event = new LinkedHashMap<>();
            event.put("type", type);
            event.put("timestamp", Instant.now().toEpochMilli());
            event.put("payload", fields);

            String json = objectMapper.writeValueAsString(event);

            TextMessage msg = session.createTextMessage(json);
            msg.setStringProperty("content-type", "application/json");
            msg.setStringProperty("eventType", type);

            producer.send(msg);
        } catch (Exception ex) {
            JMSException jmse = new JMSException("Failed to serialize event to JSON: " + ex.getMessage());
            jmse.setLinkedException(ex);
            throw jmse;
        }
    }

    @Override
    public void close() {
        try { producer.close(); } catch (Exception ignored) {}
        try { session.close(); } catch (Exception ignored) {}
        try { connection.close(); } catch (Exception ignored) {}
    }
}