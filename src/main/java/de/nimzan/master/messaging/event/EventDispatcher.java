package de.nimzan.master.messaging.event;

import de.nimzan.master.resources.NodeScorer;
import de.nimzan.master.rest.persistence.entity.NodeEntity;
import de.nimzan.master.rest.persistence.entity.ServerEntity;
import de.nimzan.master.rest.persistence.repository.NodeEntityRepository;
import de.nimzan.master.rest.persistence.repository.ServerEntityRepository;
import de.nimzan.master.scheduling.CloudCommandService;
import de.nimzan.node.enums.NodeStatus;
import jakarta.jms.JMSException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Service
public class EventDispatcher {

    @Autowired
    private NodeEntityRepository nodeRepo;

    @Autowired
    private ServerEntityRepository serverRepo;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventDispatcher.class);
    private final NodeScorer nodeScorer = new NodeScorer();
    private final CloudCommandService cloudCommandService = new CloudCommandService();
    private static final ConcurrentHashMap<UUID, NodeLogState> logState = new ConcurrentHashMap<>();

    private static final long LOG_INTERVAL_MS = 30_000;
    private static final class NodeLogState {
        final AtomicLong lastLogTime = new AtomicLong(0);
        final LongAdder count = new LongAdder();
    }

    public EventDispatcher() throws JMSException {}

    public void dispatch(Event event) throws JMSException {
        switch (event) {
            case Event.NodeHeartbeat heartbeat -> {
                NodeEntity node = new NodeEntity();

                node.setUuid(heartbeat.nodeId());
                node.setIp(heartbeat.nodeIp());
                node.setStatus(heartbeat.status());

                Set<ServerEntity> serverEntities = getServerEntities(heartbeat);
                serverRepo.saveAll(serverEntities);
                node.setServers(serverEntities);

                node.setRunningServers(heartbeat.runningServers());
                node.setFreeMemory(heartbeat.freeMemory());
                node.setTotalMemory(heartbeat.totalMemory());
                node.setMaxMemory(heartbeat.maxMemory());

                node.setCpuLoad(heartbeat.cpuLoad());
                node.setProcessCpuLoad(heartbeat.processCpuLoad());
                node.setAvailableProcessors(heartbeat.availableProcessors());
                node.setScore(nodeScorer.computeScore(node));

                if (heartbeat.status() == NodeStatus.STARTING) {
                    node.setRegistered(true);
                    cloudCommandService.sendNodeStatusUpdate(node, NodeStatus.RUNNING);
                    nodeRepo.save(node);
                    log.info("Node {} registered", heartbeat.nodeId());
                } else {
                    nodeRepo.save(node);

                    NodeLogState state = logState.computeIfAbsent(heartbeat.nodeId(), id -> new NodeLogState());
                    state.count.increment();

                    long now = System.currentTimeMillis();
                    long last = state.lastLogTime.get();

                    if (now - last > LOG_INTERVAL_MS && state.lastLogTime.compareAndSet(last, now)) {
                        long n = state.count.sumThenReset();
                        log.info("Node {} has sent {} heartbeats in the last {} ms. Last heartbeat: {}", heartbeat.nodeId(), n, (now - last)/1000 , heartbeat);
                    }

                }

            }
            case Event.NodeOffline nodeOffline -> {
                /*
                  TODO:
                  Remove Node / Deregister
                 */
                nodeRepo.deleteById(nodeOffline.nodeId());
                log.info("Node {} went offline", nodeOffline.nodeId());
            }
            case Event.NodeUpdate update -> {
                /*
                  WHAT DO I DO WITH YOU?
                 */
                log.info("Node {} updated its status to {}", update.nodeId(), update.status());
            }
            case Event.ServerStarted serverStarted -> {
                /*
                  TODO:
                  Store Server in DB
                 */
                log.info("Server started: {}", serverStarted);

                ServerEntity server = new ServerEntity();
                server.setUuid(serverStarted.serverId());
                server.setPort(serverStarted.port());
                server.setIp(serverStarted.nodeIp());
                server.setTemplate(serverStarted.template());
                server.setStatus(serverStarted.status());

                NodeEntity node = nodeRepo.findById(serverStarted.nodeId()).orElse(null);

                server.setNode(node);

                serverRepo.save(server);
            }
            case Event.ServerStopped serverStopped -> {
                /*
                  TODO:
                  Remove Server from DB
                 */
                log.info("Server stopped: {}", serverStopped);
                serverRepo.deleteById(serverStopped.serverId());
            }
            case Event.ServerStartFailed serverStartFailed -> {
                /*
                  TODO:
                  Retry Server Start IF:
                  - TemplatePolicy is not achived
                 */
            }
            case Event.ServerStopFailed serverStopFailed -> {
                /*
                  TODO: LOG
                 */
                log.info("Error stopping server: {} - {}", serverStopFailed.serverId(), serverStopFailed.reason());
            }
            case Event.ServerStatus serverStatus -> {
                /*
                  TODO:
                  - Update SINGLE SERVER Data in DB
                  - LOG
                 */
            }
            case Event.ServerStatusUpdate serverStatusUpdate -> {
                /*
                  uHM DO SOMETHING IG
                 */
                log.info("Server status update: {}", serverStatusUpdate);
            }
            case Event.ServersList serversList -> {
                /*
                  TODO:
                  - Update SERVER Data in DB
                  - LOG
                 */
            }
            case Event.Unknown u -> System.out.println("Unknown event: " + u.payload());
        }
    }

    private static Set<ServerEntity> getServerEntities(Event.NodeHeartbeat heartbeat) {
        Set<ServerEntity> serverEntities = new java.util.HashSet<>();
        for (Event.NodeHeartbeat.ServerSnapshot server : heartbeat.servers()) {
            ServerEntity entity = new ServerEntity();
            entity.setUuid(server.serverId());
            entity.setPort(server.port());
            entity.setTemplate(server.template());
            entity.setStatus(server.status());
            entity.setPort(server.port());
            entity.setNode(server.node());

            serverEntities.add(entity);
        }
        return serverEntities;
    }
}
