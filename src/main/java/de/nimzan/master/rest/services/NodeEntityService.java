package de.nimzan.master.rest.services;

import de.nimzan.master.rest.persistence.entity.NodeEntity;
import de.nimzan.master.rest.persistence.repository.NodeEntityRepository;
import de.nimzan.master.scheduling.CloudCommandService;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NodeEntityService {
    @Autowired
    private NodeEntityRepository nodeEntityRepository;

    public List<NodeEntity> getAll() {
        return nodeEntityRepository.findAll();
    }

    public Optional<NodeEntity> getById(UUID id) {
        return nodeEntityRepository.findById(id);
    }

    public boolean shutdown(UUID id) throws JMSException {
        NodeEntity node = nodeEntityRepository.findById(id).orElse(null);

        if(node == null) return false;

        CloudCommandService cloudCommandService = new CloudCommandService();
        cloudCommandService.shutdownNode(node);
        nodeEntityRepository.deleteById(id);
        return true;
    }

}
