package de.nimzan.master.rest.controller;

import de.nimzan.master.rest.persistence.entity.NodeEntity;
import de.nimzan.master.rest.services.NodeEntityService;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nodes")
public class NodeEntityController {

    @Autowired
    private NodeEntityService service;

    /**
     * Get all nodes
     */
    @GetMapping
    public ResponseEntity<List<NodeEntity>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Get node by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NodeEntity> getById(@PathVariable UUID id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Shutdown a node and delete it
     */
    @PostMapping("/{id}/shutdown")
    public ResponseEntity<Void> shutdown(@PathVariable UUID id) {
        try {
            boolean success = service.shutdown(id);
            if (!success) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}