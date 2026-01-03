package de.nimzan.master.rest.persistence.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.nimzan.node.enums.NodeStatus;
import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "master_nodes")
public class NodeEntity {
    @Id
    private UUID uuid;
    private String ip;
    
    @Enumerated(EnumType.STRING)
    private NodeStatus status;
    
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<ServerEntity> servers;
    
    private int runningServers;

    private long freeMemory;
    private long totalMemory;
    private long maxMemory;

    private double cpuLoad;
    private double processCpuLoad;
    private int availableProcessors;

    private float score;

    private boolean registered;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public Set<ServerEntity> getServers() {
        return servers;
    }

    public void setServers(Set<ServerEntity> servers) {
        this.servers = servers;
    }

    public int getRunningServers() {
        return runningServers;
    }

    public void setRunningServers(int runningServers) {
        this.runningServers = runningServers;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
