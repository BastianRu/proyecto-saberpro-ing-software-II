package com.unicauca.lisw2t2g08.banco.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados validos del ciclo de vida de una pregunta del banco (HU04).
 *
 * Cada estado conoce las transiciones que permite hacia otros estados,
 * modelando la maquina de estados de la pregunta.
 */
public enum QuestionState {

    BORRADOR("Borrador"),
    PENDIENTE_REVISION("Pendiente de revisión"),
    EN_REVISION("En revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    PUBLICADA("Publicada"),
    ARCHIVADA("Archivada");

    private final String label;

    QuestionState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Transiciones permitidas desde este estado.
     *
     * @return conjunto de estados a los que se puede pasar desde este.
     */
    public Set<QuestionState> allowedTransitions() {
        return switch (this) {
            case BORRADOR -> EnumSet.of(PENDIENTE_REVISION);
            case PENDIENTE_REVISION -> EnumSet.of(EN_REVISION, BORRADOR);
            case EN_REVISION -> EnumSet.of(APROBADA, RECHAZADA);
            case RECHAZADA -> EnumSet.of(BORRADOR, ARCHIVADA);
            case APROBADA -> EnumSet.of(PUBLICADA, ARCHIVADA);
            case PUBLICADA -> EnumSet.of(ARCHIVADA);
            case ARCHIVADA -> EnumSet.noneOf(QuestionState.class);
        };
    }

    /**
     * @param target estado destino.
     * @return true si la transicion desde este estado hacia target es valida.
     */
    public boolean canTransitionTo(QuestionState target) {
        return target != null && allowedTransitions().contains(target);
    }

    public boolean isFinal() {
        return allowedTransitions().isEmpty();
    }

    @Override
    public String toString() {
        return label;
    }
}
