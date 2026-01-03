package de.nimzan.master.rest.controller;

import de.nimzan.master.rest.persistence.entity.TemplatePolicyEntity;
import de.nimzan.master.rest.services.TemplatePolicyEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/template-policies")
public class TemplatePolicyEntityController {
    @Autowired
    private TemplatePolicyEntityService service;

    @GetMapping
    public List<TemplatePolicyEntity> getAll(

    ) {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public TemplatePolicyEntity getById(@PathVariable UUID id) {
        return service.getById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<TemplatePolicyEntity> create(@RequestBody TemplatePolicyEntity entity) {
        TemplatePolicyEntity created = service.add(entity);

        return ResponseEntity
                .created(URI.create("/api/template-policies/" + created.getUuid()))
                .body(created);
    }

    @PutMapping("/{id}")
    public TemplatePolicyEntity update(
            @PathVariable UUID id,
            @RequestBody TemplatePolicyEntity entity
    ) {
        return
                service.update(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
