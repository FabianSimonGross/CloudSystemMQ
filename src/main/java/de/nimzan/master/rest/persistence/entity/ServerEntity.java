package de.nimzan.master.rest.persistence.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "master_servers")
public class ServerEntity {
    @Id
    private UUID uuid;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    @JsonBackReference
    private NodeEntity node;

    @Enumerated(EnumType.STRING)
    private GameServerStatus status;

    @Enumerated(EnumType.STRING)
    private GameServerTemplate template;

    private String ip;

    private int port;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public NodeEntity getNode() {
        return node;
    }

    public void setNode(NodeEntity node) {
        this.node = node;
    }

    public GameServerStatus getStatus() {
        return status;
    }

    public void setStatus(GameServerStatus status) {
        this.status = status;
    }

    public GameServerTemplate getTemplate() {
        return template;
    }

    public void setTemplate(GameServerTemplate template) {
        this.template = template;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
