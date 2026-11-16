package com.unicauca.lisw2t2g08.banco.service;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;

/**
 * Contrato de negocio para la gestion del banco de preguntas y su ciclo de vida.
 */
public interface IQuestionService {

    List<Question> listQuestions();

    List<Question> searchQuestions(QuestionCriteria criteria);

    Optional<Question> findById(String id);

    Question createQuestion(QuestionDraft draft, String username);

    Question updateQuestion(String id, QuestionDraft draft, String username);

    boolean changeState(String id, QuestionState newState, String username);

    List<QuestionStateChange> stateHistory(String id);

    QuestionStatistics getStatistics();
}
