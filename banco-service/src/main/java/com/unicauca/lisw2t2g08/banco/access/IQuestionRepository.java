package com.unicauca.lisw2t2g08.banco.access;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;

/**
 * Contrato de persistencia para el banco de preguntas.
 *
 * Sigue el principio de inversion de dependencias (DIP): la capa de servicio
 * depende de esta abstraccion y no de JDBC ni de SQLite directamente.
 */
public interface IQuestionRepository {

    List<Question> findAll();

    Optional<Question> findById(String id);

    boolean save(Question question);

    boolean update(Question question);

    void saveStateChange(QuestionStateChange change);

    List<QuestionStateChange> findStateHistory(String questionId);
}
