package de.nimzan.master.messaging.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventEnvelope(
        @JsonProperty("type") String type,
        @JsonProperty("timestamp") long timestamp,
        @JsonProperty("payload") Event payload
) {
}
