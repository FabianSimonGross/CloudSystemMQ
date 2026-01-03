package de.nimzan.master.rest.services;

import de.nimzan.master.rest.persistence.entity.TemplatePolicyEntity;
import de.nimzan.master.rest.persistence.repository.TemplatePolicyEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TemplatePolicyEntityService {
    @Autowired
    private TemplatePolicyEntityRepository templatePolicyEntityRepository;

    public List<TemplatePolicyEntity> getAll() {
        return templatePolicyEntityRepository.findAll();
    }

    public Optional<TemplatePolicyEntity> getById(UUID id) {
        return templatePolicyEntityRepository.findById(id);
    }

    public TemplatePolicyEntity add(TemplatePolicyEntity entity) {
        return templatePolicyEntityRepository.save(entity);
    }

    public TemplatePolicyEntity update(TemplatePolicyEntity entity) {
        TemplatePolicyEntity template = templatePolicyEntityRepository.findById(entity.getUuid()).orElse(null);
        if (template == null) return null;

        template.setTemplate(entity.getTemplate());
        template.setMinServers(entity.getMinServers());
        template.setMaxServers(entity.getMaxServers());
        template.setEnabled(entity.isEnabled());

        return templatePolicyEntityRepository.save(template);
    }

    public boolean delete(UUID id) {
        TemplatePolicyEntity byId = templatePolicyEntityRepository.findById(id).orElse(null);
        if(byId == null) return false;
        templatePolicyEntityRepository.delete(byId);
        return true;
    }
}
