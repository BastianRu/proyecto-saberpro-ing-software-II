package com.unicauca.lisw2t2g08.revision.domain;

/** Dictamen del revisor; determina la transición que se pide al Banco. */
public enum ReviewDecision {
    APROBAR("APROBADA"),
    RECHAZAR("RECHAZADA");

    private final String targetQuestionState;

    ReviewDecision(String targetQuestionState) {
        this.targetQuestionState = targetQuestionState;
    }

    /** @return código del estado de la pregunta al que lleva este dictamen. */
    public String targetQuestionState() {
        return targetQuestionState;
    }
}
