package com.unicauca.lisw2t2g08.banco.api;

import java.util.List;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;

/**
 * Representacion publica de una pregunta en las respuestas de la API REST.
 */
public record QuestionResponse(
        String id,
        String name,
        String context,
        String statement,
        List<String> options,
        int correctIndex,
        String correctAnswer,
        QuestionState state,
        String stateLabel,
        String justification,
        String bibliography,
        String competence,
        String topic,
        String subtopic,
        DifficultyLevel difficulty) {

    public static QuestionResponse of(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getName(),
                question.getContext(),
                question.getStatement(),
                question.getDistractors().getOptions(),
                question.getDistractors().getCorrectIndex(),
                question.getCorrectAnswer(),
                question.getState(),
                question.getState().getLabel(),
                question.getJustification(),
                question.getBibliography(),
                question.getCompetence(),
                question.getTopic(),
                question.getSubtopic(),
                question.getDifficulty());
    }
}
