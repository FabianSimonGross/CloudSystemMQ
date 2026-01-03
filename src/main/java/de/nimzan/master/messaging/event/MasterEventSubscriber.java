package de.nimzan.master.messaging.event;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MasterEventSubscriber {

    private final EventDispatcher dispatcher;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MasterEventSubscriber.class);

    public MasterEventSubscriber(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @JmsListener(
            destination = "cloud.events",
            containerFactory = "topicListenerFactory"
    )
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage tm) {
                String raw = tm.getText();
                EventEnvelope envelope = EventParser.parse(raw);

                dispatcher.dispatch(envelope.payload());
                return;
            }

            ObjectNode err = JsonNodeFactory.instance.objectNode();
            err.put("error", "Unexpected JMS message type: " + message.getClass().getName());
            err.put("eventType", message.getStringProperty("eventType"));

            dispatcher.dispatch(new Event.Unknown((err)));
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new RuntimeException(ex);
        }
    }
}
