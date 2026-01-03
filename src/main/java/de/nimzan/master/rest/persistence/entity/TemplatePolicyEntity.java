package de.nimzan.master.rest.persistence.entity;

import de.nimzan.node.enums.GameServerTemplate;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "master_template_policys")
public class TemplatePolicyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    private GameServerTemplate template;

    private int minServers;
    private int maxServers;

    private boolean enabled;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public GameServerTemplate getTemplate() {
        return template;
    }

    public void setTemplate(GameServerTemplate template) {
        this.template = template;
    }

    public int getMinServers() {
        return minServers;
    }

    public void setMinServers(int minServers) {
        this.minServers = minServers;
    }

    public int getMaxServers() {
        return maxServers;
    }

    public void setMaxServers(int maxServers) {
        this.maxServers = maxServers;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
