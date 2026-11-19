package com.unicauca.lisw2t2g08.banco.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unicauca.lisw2t2g08.banco.domain.Question;

/**
 * Abstracción de la persistencia de preguntas (DIP). El servicio depende de
 * esta interfaz y no de SQLite.
 */
public interface IQuestionRepository {

    List<Question> findAll();

    Optional<Question> findById(String id);

    /** @return true si se almacenó, false si ya existía una con el mismo id. */
    boolean save(Question question);

    /** @return true si se actualizó, false si no existía. */
    boolean update(Question question);

    /** @return cantidad de preguntas por código de estado (solo los estados con al menos una). */
    Map<String, Integer> countByState();
}
