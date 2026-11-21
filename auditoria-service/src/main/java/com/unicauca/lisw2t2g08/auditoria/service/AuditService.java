package com.unicauca.lisw2t2g08.auditoria.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unicauca.lisw2t2g08.auditoria.access.IAuditRepository;
import com.unicauca.lisw2t2g08.auditoria.domain.AuditRecord;
import com.unicauca.lisw2t2g08.auditoria.events.QuestionEventMessage;

/** Casos de uso de Auditoría: registrar los eventos del Banco y consultar el historial. */
public class AuditService {

    public static final String QUESTION_CREATED_KEY = "question.created";
    public static final String QUESTION_STATE_CHANGED_KEY = "question.state-changed";

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final Map<String, String> EVENT_TYPES = Map.of(
            QUESTION_CREATED_KEY, "QuestionCreated",
            QUESTION_STATE_CHANGED_KEY, "QuestionStateChanged");

    private final IAuditRepository repository;

    public AuditService(IAuditRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra un evento. Es idempotente: el broker garantiza entrega "al menos una vez", así que el
     * mismo evento puede llegar duplicado y solo se guarda la primera vez.
     *
     * @param routingKey clave de enrutamiento con la que llegó, que determina el tipo de evento.
     * @return true si se registró; false si era duplicado o de un tipo desconocido.
     */
    public boolean record(String routingKey, QuestionEventMessage message) {
        String type = EVENT_TYPES.get(routingKey);
        if (type == null) {
            log.warn("Evento ignorado: clave de enrutamiento desconocida '{}'", routingKey);
            return false;
        }
        boolean saved = repository.saveIfNew(new AuditRecord(message.eventId(), message.questionId(), type,
                message.previousState(), message.newState(), message.username(), message.occurredAt()));
        if (!saved) {
            log.info("Evento duplicado descartado: {}", message.eventId());
        }
        return saved;
    }

    public List<AuditRecord> history(String questionId) {
        return repository.findByQuestionId(questionId);
    }
}
