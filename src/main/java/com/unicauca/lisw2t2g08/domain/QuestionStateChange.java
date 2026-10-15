package com.unicauca.lisw2t2g08.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Capa de dominio.
 *
 * Objeto de valor inmutable que registra un cambio de estado de una pregunta en
 * el historial de auditoria (criterio de aceptacion de HU04-01): guarda el
 * estado de origen, el de destino, el usuario que lo realizo y la fecha.
 */
public class QuestionStateChange {

    private final String questionId;
    private final QuestionState fromState;
    private final QuestionState toState;
    private final String username;
    private final LocalDateTime changedAt;

    /**
     * @param questionId identificador de la pregunta afectada.
     * @param fromState  estado anterior (puede ser null en altas).
     * @param toState    estado nuevo; obligatorio.
     * @param username   usuario que realizo el cambio; si falta se registra "sistema".
     * @param changedAt  fecha y hora del cambio; si es null se toma el instante actual.
     */
    public QuestionStateChange(String questionId, QuestionState fromState, QuestionState toState,
                               String username, LocalDateTime changedAt) {
        if (questionId == null || questionId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la pregunta es obligatorio");
        }
        if (toState == null) {
            throw new IllegalArgumentException("El estado destino es obligatorio");
        }
        this.questionId = questionId;
        this.fromState = fromState;
        this.toState = toState;
        this.username = (username == null || username.isBlank()) ? "sistema" : username.trim();
        this.changedAt = (changedAt == null) ? LocalDateTime.now() : changedAt;
    }

    public String getQuestionId() {
        return questionId;
    }

    public QuestionState getFromState() {
        return fromState;
    }

    public QuestionState getToState() {
        return toState;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    /**
     * @return texto legible del cambio, para mostrarlo en la interfaz.
     */
    public String getDescription() {
        String from = (fromState == null) ? "-" : fromState.getLabel();
        return from + " -> " + toState.getLabel() + "  (" + username + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof QuestionStateChange)) {
            return false;
        }
        QuestionStateChange that = (QuestionStateChange) other;
        return questionId.equals(that.questionId)
                && fromState == that.fromState
                && toState == that.toState
                && username.equals(that.username)
                && changedAt.equals(that.changedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, fromState, toState, username, changedAt);
    }

    @Override
    public String toString() {
        return changedAt + "  " + getDescription();
    }
}
