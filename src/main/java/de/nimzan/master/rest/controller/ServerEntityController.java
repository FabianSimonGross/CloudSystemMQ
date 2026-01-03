package de.nimzan.master.rest.controller;

import de.nimzan.master.rest.persistence.entity.ServerEntity;
import de.nimzan.master.rest.services.ServerEntityService;
import de.nimzan.node.enums.GameServerStatus;
import de.nimzan.node.enums.GameServerTemplate;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers")
public class ServerEntityController {

    @Autowired
    private ServerEntityService serverEntityService;

    /**
     * Alle Server abrufen
     */
    @GetMapping
    public ResponseEntity<List<ServerEntity>> getAll() {
        return ResponseEntity.ok(serverEntityService.getAll());
    }

    /**
     * Server nach Template abrufen
     */
    @GetMapping("/template/{template}")
    public ResponseEntity<List<ServerEntity>> getByTemplate(@PathVariable GameServerTemplate template) {
        return ResponseEntity.ok(serverEntityService.getByTemplate(template));
    }

    /**
     * Neuen Server anfordern
     */
    @PostMapping("/request/{template}")
    public ResponseEntity<Void> request(@PathVariable GameServerTemplate template) {
        try {
            serverEntityService.requestNewServer(template);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Server stoppen per UUID
     */
    @PostMapping("/{uuid}/stop")
    public ResponseEntity<Void> stopById(@PathVariable UUID uuid) {
        try {
            serverEntityService.stopServerById(uuid);
            // Service macht "silent return" wenn nicht gefunden -> wir bleiben konsistent bei 200
            return ResponseEntity.ok().build();
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{uuid}/update/{status}")
    public ResponseEntity<Void> updateById(@PathVariable UUID uuid, @PathVariable GameServerStatus status) {
        try {
            serverEntityService.updateStatus(uuid, status);
            return ResponseEntity.ok().build();
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
