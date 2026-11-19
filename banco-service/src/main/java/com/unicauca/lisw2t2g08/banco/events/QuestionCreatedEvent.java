package com.unicauca.lisw2t2g08.banco.events;

import java.time.Instant;

/**
 * Evento asíncrono: se creó una pregunta. Lo consumen Auditoría (y futuros servicios).
 * El eventId permite al consumidor descartar entregas duplicadas.
 */
public record QuestionCreatedEvent(String eventId, String questionId, String username, Instant occurredAt) {
}
