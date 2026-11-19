package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: Rechazada. */
final class RechazadaState extends QuestionState {

    RechazadaState() {
        super("RECHAZADA", "Rechazada");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of(QuestionStates.BORRADOR,QuestionStates.ARCHIVADA);
    }

    @Override
    void enter(Question question) {
        question.getState().reject(question);
    }

    @Override
    void returnToDraft(Question question) {
        question.moveTo(QuestionStates.BORRADOR);
    }

    @Override
    void archive(Question question) {
        question.moveTo(QuestionStates.ARCHIVADA);
    }
}
