package de.nimzan.master.messaging.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EventParser {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private EventParser() {}

    public static EventEnvelope parse(String raw) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(raw);

        String type = root.path("type").asText(null);
        long timestamp = root.path("timestamp").asLong(0);

        JsonNode payloadNode = root.path("payload");

        // inject type into the payload so Event can use As.PROPERTY
        if (type != null && payloadNode instanceof ObjectNode obj) {
            obj.put("type", type);
        }

        Event payload = objectMapper.treeToValue(payloadNode, Event.class);

        return new EventEnvelope(type, timestamp, payload);
    }
}
