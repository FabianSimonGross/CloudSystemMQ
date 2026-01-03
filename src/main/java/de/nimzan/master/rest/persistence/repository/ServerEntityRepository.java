package de.nimzan.master.rest.persistence.repository;

import de.nimzan.master.rest.persistence.entity.ServerEntity;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServerEntityRepository extends JpaRepository<ServerEntity, UUID> {
    int countByTemplateAndStatusIn(GameServerTemplate template, List<GameServerStatus> statuses);
    ArrayList<ServerEntity> findAllByTemplate(GameServerTemplate template);
}
