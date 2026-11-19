package com.unicauca.lisw2t2g08.banco.service;

import java.util.List;

import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;

/** Casos de uso del banco de preguntas (HU02, HU03 y HU04). */
public interface IQuestionService {

    /**
     * Crea una pregunta en Borrador. El borrador debe superar la validación estructural.
     *
     * @throws com.unicauca.lisw2t2g08.banco.pipeline.ValidationException si incumple alguna regla.
     */
    Question createQuestion(QuestionDraft draft, String username);

    /**
     * Modifica una pregunta en Borrador; los nuevos datos pasan la misma validación.
     *
     * @throws QuestionNotFoundException si no existe.
     * @throws com.unicauca.lisw2t2g08.banco.domain.InvalidStateOperationException si no está en Borrador.
     */
    Question updateQuestion(String id, QuestionDraft draft);

    Question getQuestion(String id);

    /** @param criteria filtros opcionales; null equivale a no filtrar. */
    List<Question> searchQuestions(QuestionCriteria criteria);

    /**
     * Cambia el estado de una pregunta y publica el evento QuestionStateChanged.
     *
     * @param targetState código del estado destino, por ejemplo "EN_REVISION".
     * @throws com.unicauca.lisw2t2g08.banco.domain.InvalidStateOperationException si la transición no es válida.
     */
    Question changeState(String id, String targetState, String username);

    QuestionStatistics getStatistics();
}
