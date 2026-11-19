package com.unicauca.lisw2t2g08.auditoria.events;

import java.time.Instant;

/**
 * Copia propia del contrato de los eventos del Banco (los servicios no comparten clases).
 * Sirve a los dos tipos: en QuestionCreated los estados llegan nulos.
 */
public record QuestionEventMessage(String eventId, String questionId, String previousState,
                                   String newState, String username, Instant occurredAt) {
}
