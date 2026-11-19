package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/** Estado concreto del patrón State: Archivada. */
final class ArchivadaState extends QuestionState {

    ArchivadaState() {
        super("ARCHIVADA", "Archivada");
    }

    @Override
    public List<QuestionState> allowedTransitions() {
        return List.of();
    }

    @Override
    void enter(Question question) {
        question.getState().archive(question);
    }
}
