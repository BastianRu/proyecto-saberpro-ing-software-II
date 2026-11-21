package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: Pendiente de revisión. */
final class PendienteRevisionState extends QuestionState {

    PendienteRevisionState() {
        super("PENDIENTE_REVISION", "Pendiente de revisión");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of(QuestionStates.EN_REVISION,QuestionStates.BORRADOR);
    }

    @Override
    void enter(Question question) {
        question.getState().submitForReview(question);
    }

    @Override
    void startReview(Question question) {
        question.moveTo(QuestionStates.EN_REVISION);
    }

    @Override
    void returnToDraft(Question question) {
        question.moveTo(QuestionStates.BORRADOR);
    }
}
