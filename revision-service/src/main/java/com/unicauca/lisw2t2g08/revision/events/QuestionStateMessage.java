package com.unicauca.lisw2t2g08.revision.events;

import java.time.Instant;

/** Copia propia del contrato del evento QuestionStateChanged del Banco (los servicios no comparten clases). */
public record QuestionStateMessage(String eventId, String questionId, String previousState,
                                   String newState, String username, Instant occurredAt) {
}
