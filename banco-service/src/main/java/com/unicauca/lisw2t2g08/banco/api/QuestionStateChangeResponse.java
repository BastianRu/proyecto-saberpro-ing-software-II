package com.unicauca.lisw2t2g08.banco.api;

import java.time.LocalDateTime;

import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;

/**
 * Registro de auditoria de cambio de estado.
 */
public record QuestionStateChangeResponse(
        String questionId,
        QuestionState fromState,
        String fromStateLabel,
        QuestionState toState,
        String toStateLabel,
        String username,
        LocalDateTime changedAt,
        String description) {

    public static QuestionStateChangeResponse of(QuestionStateChange change) {
        return new QuestionStateChangeResponse(
                change.getQuestionId(),
                change.getFromState(),
                change.getFromState() == null ? null : change.getFromState().getLabel(),
                change.getToState(),
                change.getToState().getLabel(),
                change.getUsername(),
                change.getChangedAt(),
                change.getDescription());
    }
}
