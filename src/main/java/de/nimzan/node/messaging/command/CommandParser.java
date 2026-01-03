package de.nimzan.node.messaging.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import de.nimzan.node.enums.NodeStatus;

import java.util.Locale;
import java.util.UUID;

public final class CommandParser {
    private CommandParser() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static NodeCommand parse(String raw) {
        if (raw == null) return new NodeCommand.Unknown("null");

        String input = raw.trim();
        if (input.isEmpty()) return new NodeCommand.Unknown(raw);

        // JSON only
        if (input.charAt(0) != '{') return new NodeCommand.Unknown(raw);

        try {
            JsonNode root = MAPPER.readTree(input);

            JsonNode typeNode = root.get("type");
            if (typeNode == null || !typeNode.isTextual())
                return new NodeCommand.Unknown(raw);

            String type = typeNode.asText().trim().toUpperCase(Locale.ROOT);

            JsonNode args = root.get("args");
            if (args == null || !args.isObject())
                args = MAPPER.createObjectNode();

            return switch (type) {
                case "SERVER_START" -> parseServerStart(args, raw);
                case "SERVER_STOP" -> parseServerStop(args, raw);
                case "SERVER_STATUS" -> parseServerStatus(args, raw);
                case "SERVER_STATUS_UPDATE" -> parseServerStatusUpdate(args, raw);
                case "SERVER_LIST" -> new NodeCommand.ServerList();
                case "NODE_STATUS" -> new NodeCommand.NodeStatus();
                case "NODE_STATUS_UPDATE" -> parseNodeStatus(args, raw);
                case "NODE_SHUTDOWN" -> new NodeCommand.NodeShutdown();
                default -> new NodeCommand.Unknown(raw);
            };

        } catch (JsonProcessingException e) {
            return new NodeCommand.Unknown(raw);
        }
    }

    private static NodeCommand parseServerStart(JsonNode args, String raw) {
        UUID serverUid = readUuid(args);
        String templateStr = readText(args, "template");

        if (serverUid == null || templateStr == null)
            return new NodeCommand.Unknown(raw);

        try {
            GameServerTemplate template =
                    GameServerTemplate.valueOf(templateStr.trim().toUpperCase(Locale.ROOT));
            return new NodeCommand.ServerStartWithId(template, serverUid);
        } catch (IllegalArgumentException ex) {
            return new NodeCommand.Unknown(raw);
        }
    }

    private static NodeCommand parseServerStop(JsonNode args, String raw) {
        UUID serverUid = readUuid(args);
        if (serverUid == null) return new NodeCommand.Unknown(raw);
        return new NodeCommand.ServerStop(serverUid);
    }

    private static NodeCommand parseServerStatus(JsonNode args, String raw) {
        UUID serverUid = readUuid(args);
        if (serverUid == null) return new NodeCommand.Unknown(raw);
        return new NodeCommand.ServerStatus(serverUid);
    }

    private static NodeCommand parseNodeStatus(JsonNode args, String raw) {
        String statusStr = readText(args, "status");

        if (statusStr == null) {
            return new NodeCommand.Unknown(raw);
        }

        try {
            NodeStatus status = NodeStatus.valueOf(statusStr.trim().toUpperCase(Locale.ROOT));
            return new NodeCommand.NodeStatusUpdate(status);
        } catch (IllegalArgumentException ex) {
            return new NodeCommand.Unknown(raw);
        }
    }

    private static NodeCommand parseServerStatusUpdate(JsonNode args, String raw) {
        UUID serverUid = readUuid(args);
        String statusStr = readText(args, "status");

        if (serverUid == null || statusStr == null) {
            return new NodeCommand.Unknown(raw);
        }

        try {
            GameServerStatus status = GameServerStatus.valueOf(statusStr.trim().toUpperCase(Locale.ROOT));
            return new NodeCommand.ServerStatusUpdate(serverUid, status);
        } catch (IllegalArgumentException ex) {
            return new NodeCommand.Unknown(raw);
        }
    }

    private static UUID readUuid(JsonNode obj) {
        String s = readText(obj, "serverUid");
        if (s == null) return null;
        try {
            return UUID.fromString(s.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String readText(JsonNode obj, String field) {
        JsonNode n = obj.get(field);
        if (n == null || !n.isTextual()) return null;
        String s = n.asText();
        return (s == null || s.isBlank()) ? null : s;
    }
}