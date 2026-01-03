package de.nimzan.master.rest.services;

import de.nimzan.master.rest.persistence.entity.ServerEntity;
import de.nimzan.master.rest.persistence.repository.NodeEntityRepository;
import de.nimzan.master.rest.persistence.repository.ServerEntityRepository;
import de.nimzan.master.scheduling.CloudCommandService;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServerEntityService {
    @Autowired
    private ServerEntityRepository serverEntityRepository;

    @Autowired
    private NodeEntityRepository nodeEntityRepository;

    private final CloudCommandService cloudCommandService = new CloudCommandService();

    public ServerEntityService() throws JMSException {
    }

    public List<ServerEntity> getAll() {
        return serverEntityRepository.findAll();
    }

    public List<ServerEntity> getByTemplate(GameServerTemplate template) {
        return serverEntityRepository.findAllByTemplate(template);
    }

    public void requestNewServer(GameServerTemplate template) throws JMSException {
        cloudCommandService.sendStartCommand(nodeEntityRepository, template);
    }

    public void stopServer(ServerEntity server) throws JMSException {
        cloudCommandService.sendStopServerCommand(server);
    }

    public void stopServerById(UUID uuid) throws JMSException {
        ServerEntity server = serverEntityRepository.findById(uuid).orElse(null);
        if(server == null) return;
        stopServer(server);
    }

    public void updateStatus(UUID uuid, GameServerStatus status) throws JMSException {
        ServerEntity server = serverEntityRepository.findById(uuid).orElse(null);
        if(server == null) return;
        server.setStatus(status);
        serverEntityRepository.save(server);
        cloudCommandService.sendServerStatusUpdate(server, status);
    }
}
