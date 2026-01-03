package de.nimzan.node.messaging.command;

import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;

import java.util.UUID;

public sealed interface NodeCommand permits
        NodeCommand.ServerStart,
        NodeCommand.ServerStartWithId,
        NodeCommand.ServerStop,
        NodeCommand.ServerStatus,
        NodeCommand.ServerList,
        NodeCommand.ServerStatusUpdate,
        NodeCommand.NodeStatus,
        NodeCommand.NodeStatusUpdate,
        NodeCommand.NodeShutdown,
        NodeCommand.Unknown {

    record ServerStart(GameServerTemplate template) implements NodeCommand {}
    record ServerStartWithId(GameServerTemplate template, UUID serverId) implements NodeCommand {}

    record ServerStop(UUID serverId) implements NodeCommand {}
    record ServerStatus(UUID serverId) implements NodeCommand {}
    record ServerList() implements NodeCommand {}
    record ServerStatusUpdate(UUID serverId, GameServerStatus status) implements NodeCommand {}

    record NodeStatus() implements NodeCommand {}
    record NodeStatusUpdate(de.nimzan.node.enums.NodeStatus status) implements NodeCommand {}
    record NodeShutdown() implements NodeCommand {}

    record Unknown(String raw) implements NodeCommand {}
}
