package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: Borrador. */
final class BorradorState extends QuestionState {

    BorradorState() {
        super("BORRADOR", "Borrador");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of(QuestionStates.PENDIENTE_REVISION);
    }

    @Override
    void enter(Question question) {
        question.getState().returnToDraft(question);
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    void submitForReview(Question question) {
        question.moveTo(QuestionStates.PENDIENTE_REVISION);
    }
}
