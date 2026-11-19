package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: En revisión. */
final class EnRevisionState extends QuestionState {

    EnRevisionState() {
        super("EN_REVISION", "En revisión");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of(QuestionStates.APROBADA,QuestionStates.RECHAZADA);
    }

    @Override
    void enter(Question question) {
        question.getState().startReview(question);
    }

    @Override
    void approve(Question question) {
        question.moveTo(QuestionStates.APROBADA);
    }

    @Override
    void reject(Question question) {
        question.moveTo(QuestionStates.RECHAZADA);
    }
}
