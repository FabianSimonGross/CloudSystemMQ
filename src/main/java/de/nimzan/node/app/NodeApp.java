package de.nimzan.node.app;

import com.sun.management.OperatingSystemMXBean;
import de.nimzan.node.messaging.command.CommandDispatcher;
import de.nimzan.node.enums.NodeStatus;
import de.nimzan.node.messaging.command.NodeCommandConsumer;
import de.nimzan.node.messaging.event.NodeEventPublisher;
import de.nimzan.node.server.GameServer;
import de.nimzan.node.service.ServerManager;
import jakarta.jms.JMSException;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class NodeApp implements Runnable {

    private final UUID nodeId = UUID.randomUUID();
    private final String nodeIp = Inet4Address.getLocalHost().getHostAddress();
    private volatile NodeStatus nodeStatus = NodeStatus.STARTING;

    private ServerManager serverManager;

    private NodeCommandConsumer consumer;

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private NodeEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NodeApp.class);

    public NodeApp() throws UnknownHostException {
    }


    @Override
    public void run() {
        try {
            log.info("Starting node {}", nodeId);
            start();
            shutdownLatch.await();
        } catch (Exception ex) {
            nodeStatus = NodeStatus.CRASHED;
            log.error("Node {} crashed: {}", nodeId, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void start() throws Exception {
        eventPublisher = new NodeEventPublisher("cloud.events");
        this.serverManager = new ServerManager(nodeId, nodeIp, eventPublisher);

        // Heartbeat alle 3 Sekunden (reicht völlig)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Runtime runtime = Runtime.getRuntime();

                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();


                HashMap<String , Object> payload = new HashMap<>();
                payload.put("nodeId", nodeId);
                payload.put("nodeIp", nodeIp);
                payload.put("status", nodeStatus);
                payload.put("runningServers", serverManager.getServers().size());
                payload.put("gameServers", serverManager.getServers().values());

                payload.put("freeMemory", runtime.freeMemory());
                payload.put("totalMemory", runtime.totalMemory());
                payload.put("maxMemory", runtime.maxMemory());

                payload.put("cpuLoad", osBean.getCpuLoad());
                payload.put("processCpuLoad", osBean.getProcessCpuLoad());
                payload.put("availableProcessors", osBean.getAvailableProcessors());

                payload.put("timestamp", System.currentTimeMillis());

                eventPublisher.publish("node.heartbeat", payload);
            } catch (Exception ignored) {}
        }, 0, 5, TimeUnit.SECONDS);


        String queueName = "node." + nodeId + ".commands";
        var dispatcher = new CommandDispatcher(
                serverManager,
                this::shutdownGracefully,
                this::setNodeStatus
        );

        consumer = new NodeCommandConsumer(queueName, dispatcher);
        consumer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownGracefully));

        log.info("Node {} is listening for commands...", nodeId);
    }

    private void shutdownGracefully() {
        scheduler.shutdownNow();

        try {
            eventPublisher.publish("node.offline", Map.of("nodeId", nodeId.toString()));
        } catch (Exception ignored) {}

        if (eventPublisher != null) eventPublisher.close();

        if (nodeStatus == NodeStatus.STOPPING) return;
        nodeStatus = NodeStatus.STOPPING;

        Collection<GameServer> servers = serverManager.getServers().values();
        for (GameServer server : servers) {
            serverManager.stopServer(server.getUuid());
        }

        if (consumer != null) consumer.close();

        shutdownLatch.countDown();
    }

    public void setNodeStatus(NodeStatus status) {
        this.nodeStatus = status;

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("nodeId", nodeId);
        payload.put("status", nodeStatus);

        try {
            eventPublisher.publish("node.update", payload);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}