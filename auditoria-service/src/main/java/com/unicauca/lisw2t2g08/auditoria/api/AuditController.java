package com.unicauca.lisw2t2g08.auditoria.api;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.unicauca.lisw2t2g08.auditoria.domain.AuditRecord;
import com.unicauca.lisw2t2g08.auditoria.service.AuditService;

/** Punto de entrada REST: consulta del historial de una pregunta. */
@RestController
public class AuditController {

    public record AuditEntry(String eventId, String eventType, String previousState, String newState,
                             String username, Instant occurredAt) {
        static AuditEntry of(AuditRecord r) {
            return new AuditEntry(r.eventId(), r.eventType(), r.previousState(), r.newState(),
                    r.username(), r.occurredAt());
        }
    }

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    // Lista vacía (no 404) si la pregunta no tiene registros: puede existir y aún no haberse procesado su evento
    @GetMapping("/auditoria/preguntas/{id}")
    public List<AuditEntry> history(@PathVariable String id) {
        return service.history(id).stream().map(AuditEntry::of).toList();
    }
}
