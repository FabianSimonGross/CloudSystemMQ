package de.nimzan.master.scheduling;

import de.nimzan.master.messaging.command.MasterCommandProducer;
import de.nimzan.master.resources.NodeScorer;
import de.nimzan.master.rest.persistence.entity.NodeEntity;
import de.nimzan.master.rest.persistence.entity.ServerEntity;
import de.nimzan.master.rest.persistence.repository.NodeEntityRepository;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import de.nimzan.node.enums.NodeStatus;
import jakarta.jms.JMSException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CloudCommandService {

    private final MasterCommandProducer producer = new MasterCommandProducer();

    public CloudCommandService() throws JMSException {
    }

    public void sendStartToSpecificNode(NodeEntity node, GameServerTemplate template) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(node),
                "SERVER_START",
                Map.of(
                        "serverUid", UUID.randomUUID(),
                        "template", template.name()
                )
        );
    }

    public void sendStartCommand(NodeEntityRepository nodeRepo, GameServerTemplate template) throws JMSException {
        List<NodeEntity> healthyNodes = nodeRepo.findAll().stream()
                .filter(nodeEntity -> nodeEntity.getStatus() == NodeStatus.RUNNING)
                .toList();

        if (healthyNodes.isEmpty()) return;

        NodeScorer nodeScorer = new NodeScorer();
        List<NodeEntity> rescoredNodes = nodeScorer.rescoreNodes(healthyNodes);
        nodeRepo.saveAll(rescoredNodes);
        NodeEntity chosenNode = nodeScorer.chooseNode(rescoredNodes);

        if (chosenNode == null) return;

        producer.sendToQueue(
                nodeCommandQueueByNode(chosenNode),
                "SERVER_START",
                Map.of(
                        "serverUid", UUID.randomUUID(),
                        "template", template.name()
                )
        );
    }

    public void sendStopServerCommand(ServerEntity server) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(server.getNode()),
                "SERVER_STOP",
                Map.of(
                        "serverUid", server.getUuid()
                )
        );
    }

    public void retrieveStatusOfServer(ServerEntity server) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(server.getNode()),
                "SERVER_STATUS",
                Map.of(
                        "serverUid", server.getUuid()
                )
        );
    }

    public void retrieveListServersCommand(NodeEntity node) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(node),
                "SERVER_LIST",
                Map.of()
        );
    }

    public void retrieveNodeStatus(NodeEntity node) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(node),
                "NODE_STATUS",
                Map.of()
        );
    }

    public void shutdownNode(NodeEntity node) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(node),
                "NODE_SHUTDOWN",
                Map.of()
        );
    }

    public void sendNodeStatusUpdate(NodeEntity node, NodeStatus status) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(node),
                "NODE_STATUS_UPDATE",
                Map.of(
                        "status", status.name()
                )
        );
    }

    public void sendServerStatusUpdate(ServerEntity server, GameServerStatus status) throws JMSException {
        producer.sendToQueue(
                nodeCommandQueueByNode(server.getNode()),
                "SERVER_STATUS_UPDATE",
                Map.of(
                        "serverUid", server.getUuid(),
                        "status", status.name()
                )
        );
    }

    public String nodeCommandQueueByNode(NodeEntity node) {
        return "node." + node.getUuid() + ".commands";
    }
}
