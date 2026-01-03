package de.nimzan.master.rest.persistence.repository;

import de.nimzan.master.rest.persistence.entity.TemplatePolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TemplatePolicyEntityRepository extends JpaRepository<TemplatePolicyEntity, UUID> {
}
