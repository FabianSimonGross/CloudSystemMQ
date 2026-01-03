package de.nimzan.master.messaging.event;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import de.nimzan.master.rest.persistence.entity.NodeEntity;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import de.nimzan.node.enums.NodeStatus;

import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = Event.Unknown.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Event.NodeHeartbeat.class, name = "node.heartbeat"),
        @JsonSubTypes.Type(value = Event.NodeOffline.class,   name = "node.offline"),
        @JsonSubTypes.Type(value = Event.NodeUpdate.class, name = "node.update"),
        @JsonSubTypes.Type(value = Event.ServerStarted.class, name = "server.start"),
        @JsonSubTypes.Type(value = Event.ServerStopped.class, name = "server.stopped"),
        @JsonSubTypes.Type(value = Event.ServerStartFailed.class, name = "server.start.failed"),
        @JsonSubTypes.Type(value = Event.ServerStopFailed.class,  name = "server.stop.failed"),
        @JsonSubTypes.Type(value = Event.ServerStatus.class,  name = "server.status"),
        @JsonSubTypes.Type(value = Event.ServerStatusUpdate.class, name = "server.status.update"),
        @JsonSubTypes.Type(value = Event.ServersList.class,   name = "server.list")
})
public sealed interface Event permits
        Event.NodeHeartbeat,
        Event.NodeOffline,
        Event.NodeUpdate,
        Event.ServerStarted,
        Event.ServerStopped,
        Event.ServerStartFailed,
        Event.ServerStopFailed,
        Event.ServerStatus,
        Event.ServerStatusUpdate,
        Event.ServersList,
        Event.Unknown {

    record NodeHeartbeat(
            UUID nodeId,
            String nodeIp,
            NodeStatus status,
            int runningServers,
            long freeMemory,
            long totalMemory,
            long maxMemory,
            double cpuLoad,
            double processCpuLoad,
            int availableProcessors,
            @JsonAlias("gameServers") Set<ServerSnapshot> servers
    ) implements Event {
    }

    record NodeOffline(UUID nodeId) implements Event {}

    record NodeUpdate(UUID nodeId, NodeStatus status) implements Event {}

    record ServerStarted(
            @JsonProperty("serverId") UUID serverId,
            @JsonProperty("nodeId") UUID nodeId,
            @JsonProperty("nodeIp") String nodeIp,
            @JsonProperty("template") GameServerTemplate template,
            @JsonProperty("status") GameServerStatus status,
            @JsonProperty("port") int port
    ) implements Event {
    }
    record ServerStopped(UUID serverId) implements Event {}

    record ServerStartFailed(UUID serverId, String reason) implements Event {}
    record ServerStopFailed(UUID serverId, String reason) implements Event {}

    record ServerStatus(UUID serverId, String status) implements Event {}
    record ServerStatusUpdate(UUID serverId, GameServerStatus status) implements Event {}
    record ServersList(Set<UUID> serverIds) implements Event {}

    record Unknown(JsonNode payload) implements Event {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Unknown {}
    }

    record ServerSnapshot(UUID serverId, int port, GameServerTemplate template, GameServerStatus status, NodeEntity node) {}
}