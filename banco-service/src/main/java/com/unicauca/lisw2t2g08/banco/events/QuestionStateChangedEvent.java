package com.unicauca.lisw2t2g08.banco.events;

import java.time.Instant;

/**
 * Evento asíncrono: cambió el estado de una pregunta. Lo consumen Auditoría y Revisión.
 * Los estados viajan como texto (código) para no acoplar a los consumidores a las clases del Banco.
 */
public record QuestionStateChangedEvent(String eventId, String questionId, String previousState,
                                        String newState, String username, Instant occurredAt) {
}
