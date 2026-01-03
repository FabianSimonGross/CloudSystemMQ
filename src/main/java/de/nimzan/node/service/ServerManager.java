package de.nimzan.node.service;

import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import de.nimzan.node.messaging.event.NodeEventPublisher;
import de.nimzan.node.resources.PortManager;
import de.nimzan.node.server.GameServer;
import de.nimzan.node.server.process.ServerProcessManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerManager {

    private final Map<UUID, GameServer> servers = new ConcurrentHashMap<>();
    private final ServerProcessManager serverProcessManager = new ServerProcessManager();
    private final PortManager portManager = new PortManager();

    private final UUID nodeId;
    private final String nodeIp;
    private final NodeEventPublisher publisher;

    public ServerManager(UUID nodeId, String nodeIp, NodeEventPublisher publisher) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.nodeIp = Objects.requireNonNull(nodeIp);
        this.publisher = publisher; // can be null in tests
    }

    public UUID startServerWithId(GameServerTemplate template, UUID serverId) throws IOException {
        int port = portManager.allocate();

        GameServer gameServer = new GameServer(serverId, template, port, nodeIp);
        servers.put(serverId, gameServer);

        try {
            gameServer.setProcess(serverProcessManager.startGameServer(gameServer));

            publishSafe("server.start", Map.of(
                    "nodeId", nodeId.toString(),
                    "nodeIp", nodeIp,
                    "serverId", serverId.toString(),
                    "template", template.name(),
                    "status", gameServer.getStatus().name(),
                    "port", port
            ));

            return serverId;
        } catch (IOException | RuntimeException ex) {
            servers.remove(serverId);
            portManager.release(port);

            publishSafe("server.start.failed", Map.of(
                    "nodeId", nodeId.toString(),
                    "nodeIp", nodeIp,
                    "serverId", serverId.toString(),
                    "template", template.name(),
                    "reason", ex.getClass().getSimpleName()
            ));

            throw ex;
        }
    }

    public UUID startServer(GameServerTemplate template) throws IOException {
        return startServerWithId(template, UUID.randomUUID());
    }

    public void stopServer(UUID serverId) {
        GameServer gameServer = servers.remove(serverId);
        if (gameServer == null) {
            publishSafe("server.stop.failed", Map.of(
                    "nodeId", nodeId.toString(),
                    "nodeIp", nodeIp,
                    "serverId", serverId.toString(),
                    "reason", "NOT_FOUND"
            ));
            return;
        }

        boolean ok = false;
        try {
            portManager.release(gameServer.getPort());
            ok = serverProcessManager.stopGameServer(gameServer);
        } finally {
            publishSafe("server.stopped", Map.of(
                    "nodeId", nodeId.toString(),
                    "nodeIp", nodeIp,
                    "serverId", serverId.toString(),
                    "ok", ok
            ));
        }
    }

    public Optional<GameServerStatus> getGameServerStatus(UUID serverId) {
        GameServer gs = servers.get(serverId);
        GameServerStatus status = (gs == null) ? null : gs.getStatus();

        publishSafe("server.status", Map.of(
                "nodeId", nodeId.toString(),
                "nodeIp", nodeIp,
                "serverId", serverId.toString(),
                "status", status == null ? "UNKNOWN" : status.name()
        ));

        return Optional.ofNullable(status);
    }

    public Map<UUID, GameServer> getServers() {
        return servers;
    }

    public void updateServerStatus(UUID serverId, GameServerStatus status) {
        servers.get(serverId).setStatus(status);

        publishSafe("server.status.update", Map.of(
                "nodeId", nodeId.toString(),
                "nodeIp", nodeIp,
                "serverId", serverId.toString(),
                "status", status.name()
        ));
    }

    public void publishServersSnapshot() {
        ArrayList<Map<String, Object>> serversJson = new ArrayList<>();

        for (GameServer value : servers.values()) {
            serversJson.add(Map.of(
                    "serverId", value.getUuid().toString(),
                    "template", value.getTemplate().name(),
                    "status", value.getStatus().name(),
                    "port", value.getPort()
            ));
        }

        publishSafe("server.list", Map.of(
                "nodeId", nodeId.toString(),
                "nodeIp", nodeIp,
                "servers", serversJson
        ));

    }

    private void publishSafe(String type, Map<String, Object> fields) {
        if (publisher == null) return;
        try {
            publisher.publish(type, fields);
        } catch (Exception ignored) {
        }
    }
}