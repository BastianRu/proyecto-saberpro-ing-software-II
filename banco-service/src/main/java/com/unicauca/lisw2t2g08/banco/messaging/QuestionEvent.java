package com.unicauca.lisw2t2g08.banco.messaging;

import java.time.LocalDateTime;

import com.unicauca.lisw2t2g08.banco.domain.QuestionState;

/**
 * Evento de dominio emitido ante cambios en el ciclo de vida o edicion de preguntas.
 */
public record QuestionEvent(
        String eventType,
        String questionId,
        QuestionState fromState,
        QuestionState toState,
        String username,
        LocalDateTime timestamp) {

    public static QuestionEvent created(String questionId, String username) {
        return new QuestionEvent("QUESTION_CREATED", questionId, null, QuestionState.BORRADOR, username, LocalDateTime.now());
    }

    public static QuestionEvent updated(String questionId, String username) {
        return new QuestionEvent("QUESTION_UPDATED", questionId, QuestionState.BORRADOR, QuestionState.BORRADOR, username, LocalDateTime.now());
    }

    public static QuestionEvent stateChanged(String questionId, QuestionState fromState, QuestionState toState, String username) {
        return new QuestionEvent("QUESTION_STATE_CHANGED", questionId, fromState, toState, username, LocalDateTime.now());
    }
}
