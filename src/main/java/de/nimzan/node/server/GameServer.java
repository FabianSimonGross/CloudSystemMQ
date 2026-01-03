package de.nimzan.node.server;

import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class GameServer {
    private UUID uuid;
    private String ip;
    private Integer port;
    private GameServerTemplate template;
    private GameServerStatus status;
    private final Path directory;
    private Process process;

    public GameServer(UUID uuid, GameServerTemplate template, Integer port, String ip) {
        this.setUuid(uuid);
        this.setTemplate(template);
        this.setStatus(GameServerStatus.REQUESTED);
        this.setPort(port);
        this.setIp(ip);

        this.directory = createDirecotryFromTemplate();
    }

    private Path createDirecotryFromTemplate() {
        Path serverDir = Paths.get("running", uuid.toString());
        Path templateDir = Paths.get(template.getPath());

        try {
            Files.createDirectories(serverDir);
            copyTemplate(templateDir, serverDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw  new RuntimeException(
                    "Failed to create server directory from template: " + template.name(), e
            );
        }

        return serverDir;
    }

    private void copyTemplate(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public GameServerTemplate getTemplate() {
        return template;
    }

    public void setTemplate(GameServerTemplate template) {
        this.template = template;
    }

    public GameServerStatus getStatus() {
        return status;
    }

    public void setStatus(GameServerStatus status) {
        this.status = status;
    }

    public Path getDirectory() {
        return directory;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}
