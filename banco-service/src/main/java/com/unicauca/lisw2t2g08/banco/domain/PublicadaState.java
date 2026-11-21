package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: Publicada. */
final class PublicadaState extends QuestionState {

    PublicadaState() {
        super("PUBLICADA", "Publicada");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of(QuestionStates.ARCHIVADA);
    }

    @Override
    void enter(Question question) {
        question.getState().publish(question);
    }

    @Override
    void archive(Question question) {
        question.moveTo(QuestionStates.ARCHIVADA);
    }
}
