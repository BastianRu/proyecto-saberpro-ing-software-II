package com.unicauca.lisw2t2g08.access;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;

/**
 * Abstraccion de la persistencia de preguntas.
 *
 * DIP: QuestionService depende de esta interfaz y no de SQLite, por lo que la
 * implementacion se puede reemplazar (o falsear en las pruebas) sin tocar la
 * logica de negocio.
 */
public interface IQuestionRepository {

    /**
     * @return todas las preguntas almacenadas.
     */
    List<Question> findAll();

    /**
     * @param id identificador de la pregunta.
     * @return la pregunta si existe.
     */
    Optional<Question> findById(String id);

    /**
     * Almacena una pregunta nueva.
     *
     * @param question pregunta a almacenar.
     * @return true si se almaceno, false si ya existia una con el mismo id.
     */
    boolean save(Question question);

    /**
     * Actualiza una pregunta existente.
     *
     * @param question pregunta con los datos modificados.
     * @return true si se actualizo, false si no existia.
     */
    boolean update(Question question);

    /**
     * Registra un cambio de estado en el historial de auditoria (HU04-01).
     *
     * @param change cambio de estado a registrar.
     */
    void saveStateChange(QuestionStateChange change);

    /**
     * @param questionId identificador de la pregunta.
     * @return historial de cambios de estado de la pregunta, del mas antiguo al
     *         mas reciente.
     */
    List<QuestionStateChange> findStateHistory(String questionId);
}
