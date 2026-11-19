package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: Aprobada. */
final class AprobadaState extends QuestionState {

    AprobadaState() {
        super("APROBADA", "Aprobada");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of(QuestionStates.PUBLICADA,QuestionStates.ARCHIVADA);
    }

    @Override
    void enter(Question question) {
        question.getState().approve(question);
    }

    @Override
    void publish(Question question) {
        question.moveTo(QuestionStates.PUBLICADA);
    }

    @Override
    void archive(Question question) {
        question.moveTo(QuestionStates.ARCHIVADA);
    }
}
