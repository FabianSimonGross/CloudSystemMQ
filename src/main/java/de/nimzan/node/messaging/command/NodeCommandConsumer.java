package de.nimzan.node.messaging.command;

import de.nimzan.node.config.NodeConfig;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class NodeCommandConsumer implements AutoCloseable {

    private final String queueName;
    private final CommandHandler handler;

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    private final AtomicBoolean started = new AtomicBoolean(false);

    public interface CommandHandler {
        void onCommand(NodeCommand command) throws IOException;
        void onError(Exception ex);
    }

    public NodeCommandConsumer(String queueName, CommandHandler handler) {
        this.queueName = Objects.requireNonNull(queueName);
        this.handler = Objects.requireNonNull(handler);
    }

    public void start() throws JMSException {
        String user = NodeConfig.activemqUser();
        String password = NodeConfig.activemqPassword();
        String url = NodeConfig.brokerUrl();
        if (!started.compareAndSet(false, true)) return;

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);

        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue destination = session.createQueue(queueName);

        consumer = session.createConsumer(destination);

        // async consumption instead of while(receive())
        consumer.setMessageListener(this::onMessage);
    }

    private void onMessage(Message message) {
        try {
            if (message instanceof TextMessage tm) {
                String raw = tm.getText();
                NodeCommand cmd = CommandParser.parse(raw);
                handler.onCommand(cmd);
            } else {
                handler.onCommand(new NodeCommand.Unknown("Unexpected message type: " + message.getClass().getName()));
            }
        } catch (Exception ex) {
            handler.onError(ex);
        }
    }

    @Override
    public void close() {
        // close in reverse order, ignore errors but notify the handler if you want
        try { if (consumer != null) consumer.close(); } catch (Exception ignored) {}
        try { if (session != null) session.close(); } catch (Exception ignored) {}
        try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        started.set(false);
    }
}
