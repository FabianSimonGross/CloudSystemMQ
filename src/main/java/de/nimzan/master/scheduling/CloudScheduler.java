package de.nimzan.master.scheduling;

import de.nimzan.master.resources.NodeScorer;
import de.nimzan.master.rest.persistence.entity.NodeEntity;
import de.nimzan.master.rest.persistence.entity.ServerEntity;
import de.nimzan.master.rest.persistence.entity.TemplatePolicyEntity;
import de.nimzan.master.rest.persistence.repository.NodeEntityRepository;
import de.nimzan.master.rest.persistence.repository.ServerEntityRepository;
import de.nimzan.master.rest.persistence.repository.TemplatePolicyEntityRepository;
import de.nimzan.node.enums.GameServerTemplate;
import de.nimzan.node.enums.NodeStatus;
import jakarta.jms.JMSException;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CloudScheduler {
    @Autowired
    private TemplatePolicyEntityRepository policyRepo;

    @Autowired
    private ServerEntityRepository serverRepo;

    @Autowired
    private NodeEntityRepository nodeRepo;

    private final ReentrantLock lock = new ReentrantLock();

    private static final int MAX_STARTS_PER_TICK = 5;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CloudScheduler.class);

    private final CloudCommandService cloudCommandService = new CloudCommandService();

    public CloudScheduler() throws JMSException {}

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void reconcile() {
        if (!lock.tryLock()) {
            return;
        }

        try {
            List<NodeEntity> healthyNodes = nodeRepo.findAll().stream().filter(node -> node.getStatus() == NodeStatus.RUNNING).toList();

            if (healthyNodes.isEmpty()) return;

            List<NodeEntity> rescoredNodes = new NodeScorer().rescoreNodes(healthyNodes);
            nodeRepo.saveAll(rescoredNodes);

            EnumMap<GameServerTemplate, TemplatePolicyEntity> policyByTemplate = new EnumMap<>(GameServerTemplate.class);
            for (TemplatePolicyEntity p : policyRepo.findAll()) {
                if (p.isEnabled()) {
                    policyByTemplate.put(p.getTemplate(), p);
                }
            }

            int startsIssued = 0;

            for (GameServerTemplate template : GameServerTemplate.values()) {
                TemplatePolicyEntity policy = policyByTemplate.get(template);

                int desiredMin = 0;
                int desiredMax = 0;

                if (policy != null) {
                    desiredMin = policy.getMinServers();
                    desiredMax = Math.max(desiredMin, policy.getMaxServers());
                }

                ArrayList<ServerEntity> servers = serverRepo.findAllByTemplate(template);
                int current = servers.size();

                int need = desiredMin - current;

                while (need > 0 && startsIssued < MAX_STARTS_PER_TICK) {
                    NodeEntity node = new NodeScorer().chooseNode(rescoredNodes);
                    if (node == null) break;

                    cloudCommandService.sendStartToSpecificNode(node, template);

                    startsIssued++;
                    need--;

                    node.setRunningServers(node.getRunningServers() + 1);
                    node.setScore(new NodeScorer().computeScore(node));
                }

                int over = current - desiredMax;
                if (over > 0) {
                    for (int i = 0; i < over && i < servers.size(); i++) {
                        cloudCommandService.sendStopServerCommand(servers.get(i));
                        serverRepo.delete(servers.get(i));
                    }
                }
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
