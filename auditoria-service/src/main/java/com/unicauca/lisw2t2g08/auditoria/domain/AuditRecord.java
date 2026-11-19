package com.unicauca.lisw2t2g08.auditoria.domain;

import java.time.Instant;

/**
 * Registro inmutable de auditoría. Los estados se guardan como texto (código) para no depender
 * de las clases del Banco. Los de un QuestionCreated no tienen estado previo ni nuevo.
 */
public record AuditRecord(String eventId, String questionId, String eventType, String previousState,
                          String newState, String username, Instant occurredAt) {

    public AuditRecord {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("El identificador del evento es obligatorio");
        }
        if (questionId == null || questionId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la pregunta es obligatorio");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        username = (username == null || username.isBlank()) ? "sistema" : username;
    }
}
