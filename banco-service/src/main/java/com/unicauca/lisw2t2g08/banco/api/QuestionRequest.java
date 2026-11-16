package com.unicauca.lisw2t2g08.banco.api;

import java.util.ArrayList;
import java.util.List;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;

/**
 * Peticion para crear o actualizar una pregunta.
 */
public record QuestionRequest(
        String name,
        String context,
        String directQuestion,
        List<String> options,
        Integer correctIndex,
        String justification,
        String bibliography,
        String competence,
        String topic,
        String subtopic,
        DifficultyLevel difficulty) {

    public QuestionDraft toDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setName(name == null ? "" : name);
        draft.setContext(context == null ? "" : context);
        draft.setDirectQuestion(directQuestion == null ? "" : directQuestion);
        draft.setOptions(options == null ? new ArrayList<>() : new ArrayList<>(options));
        draft.setCorrectIndex(correctIndex == null ? -1 : correctIndex);
        draft.setJustification(justification == null ? "" : justification);
        draft.setBibliography(bibliography == null ? "" : bibliography);
        draft.setCompetence(competence == null ? "" : competence);
        draft.setTopic(topic == null ? "" : topic);
        draft.setSubtopic(subtopic == null ? "" : subtopic);
        draft.setDifficulty(difficulty);
        return draft;
    }
}
