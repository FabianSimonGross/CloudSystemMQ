package de.nimzan.node.messaging.command;

import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.NodeStatus;
import de.nimzan.node.server.GameServer;
import de.nimzan.node.service.ServerManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class CommandDispatcher implements NodeCommandConsumer.CommandHandler {

    private final ServerManager serverManager;
    private final Runnable onExit;
    private final Consumer<NodeStatus> setNodeStatus;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    public CommandDispatcher(ServerManager serverManager, Runnable onExit, Consumer<NodeStatus> setNodeStatus) {
        this.serverManager = serverManager;
        this.onExit = onExit;
        this.setNodeStatus = setNodeStatus;
    }

    @Override
    public void onCommand(NodeCommand command) throws IOException {
        switch (command) {
            case NodeCommand.ServerStart start -> {
                UUID id = serverManager.startServer(start.template());
                log.info("Started server {} with template {}", id, start.template());
            }
            case NodeCommand.ServerStartWithId start -> {
                serverManager.startServerWithId(start.template(), start.serverId());
                log.info("Started server with provided id {} with template {}", start.serverId(), start.template());
            }
            case NodeCommand.ServerStop stop -> {
                log.info("Stopped server {} of template {}", stop.serverId(), serverManager.getServers().get(stop.serverId()).getTemplate());
                serverManager.stopServer(stop.serverId());
            }
            case NodeCommand.ServerStatus status -> {
                Optional<GameServerStatus> s = serverManager.getGameServerStatus(status.serverId());
                log.info("Status of server {}: {}", status.serverId(), s);
            }
            case NodeCommand.ServerStatusUpdate update -> {
                serverManager.updateServerStatus(update.serverId(), update.status());
                log.info("Status of server {} updated to {}", update.serverId(), update.status());
            }
            case NodeCommand.ServerList ignored -> {
                for (GameServer gs : serverManager.getServers().values()) {
                    log.info("{} : {} / {} / {}", gs.getUuid(), gs.getTemplate().name(), gs.getStatus(), gs.getPort());
                }
                serverManager.publishServersSnapshot();
            }
            case NodeCommand.NodeStatus nodeStatus -> {
                log.info("Status of Node: {}", nodeStatus);
            }
            case NodeCommand.NodeStatusUpdate update -> {
                setNodeStatus.accept(update.status());
                log.info("Status of Node updated to {}", update.status());
            }
            case NodeCommand.NodeShutdown ignored -> {
                log.info("Shutting down...");
                onExit.run();
            }
            case NodeCommand.Unknown u -> log.info("Unknown command: {}", u.raw());
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("Command consumer error: {} / {}", ex.getMessage(), ex.getStackTrace());
    }
}
