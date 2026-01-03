package de.nimzan.master.rest.persistence.repository;

import de.nimzan.master.rest.persistence.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NodeEntityRepository extends JpaRepository<NodeEntity, UUID> {
}
