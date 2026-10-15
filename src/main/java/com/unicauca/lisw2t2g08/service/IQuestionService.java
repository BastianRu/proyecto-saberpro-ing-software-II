package com.unicauca.lisw2t2g08.service;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;

/**
 * Abstraccion del servicio de preguntas, es decir, del modelo del micro patron
 * MVC. Declara unicamente las operaciones de negocio que la capa de
 * presentacion necesita.
 *
 * DIP: el controlador depende de esta interfaz y no de la implementacion
 * concreta. ISP: los metodos de suscripcion de observadores quedan en Subject,
 * fuera de este contrato.
 */
public interface IQuestionService {

    /**
     * @return todas las preguntas del banco.
     */
    List<Question> listQuestions();

    /**
     * @param id identificador de la pregunta.
     * @return la pregunta si existe.
     */
    Optional<Question> findById(String id);

    /**
     * Registra una pregunta nueva.
     *
     * @param question pregunta a registrar.
     * @return true si se registro.
     */
    boolean registerQuestion(Question question);

    /**
     * RF-04: crea una pregunta nueva en estado Borrador. El borrador debe
     * superar la validacion estructural (RF-05, RF-08 a RF-13).
     *
     * @param draft datos de la pregunta.
     * @return la pregunta creada, con su id asignado.
     * @throws com.unicauca.lisw2t2g08.pipeline.ValidationException si incumple alguna regla.
     */
    Question createQuestion(QuestionDraft draft);

    /**
     * RF-06: modifica una pregunta que este en estado Borrador. Los nuevos
     * datos deben superar la misma validacion estructural que al crear.
     *
     * @param id    identificador de la pregunta.
     * @param draft nuevos datos de la pregunta.
     * @return la pregunta modificada.
     * @throws IllegalStateException si la pregunta no existe o no esta en Borrador.
     * @throws com.unicauca.lisw2t2g08.pipeline.ValidationException si incumple alguna regla.
     */
    Question updateQuestion(String id, QuestionDraft draft);

    /**
     * RF-07: consulta las preguntas que cumplen los criterios indicados.
     *
     * @param criteria filtros opcionales; null equivale a no filtrar.
     * @return preguntas que cumplen todos los filtros informados.
     */
    List<Question> searchQuestions(QuestionCriteria criteria);

    /**
     * Cambia el estado de una pregunta, registra el cambio en el historial de
     * auditoria (usuario y fecha) y notifica a las vistas observadoras.
     *
     * @param id       identificador de la pregunta.
     * @param newState estado destino.
     * @param username usuario que realiza el cambio; si falta se registra "sistema".
     * @return true si el estado se actualizo (o ya era el indicado).
     * @throws IllegalStateException si la transicion no es valida.
     */
    boolean changeState(String id, QuestionState newState, String username);

    /**
     * Cambia el estado de una pregunta sin usuario explicito; el cambio queda
     * registrado a nombre de "sistema".
     *
     * @param id       identificador de la pregunta.
     * @param newState estado destino.
     * @return true si el estado se actualizo (o ya era el indicado).
     * @throws IllegalStateException si la transicion no es valida.
     */
    default boolean changeState(String id, QuestionState newState) {
        return changeState(id, newState, "sistema");
    }

    /**
     * @param id identificador de la pregunta.
     * @return historial de cambios de estado de la pregunta.
     */
    List<QuestionStateChange> stateHistory(String id);

    /**
     * @return estadisticas actuales del banco de preguntas.
     */
    QuestionStatistics getStatistics();
}
