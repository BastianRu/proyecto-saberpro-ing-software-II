package com.unicauca.lisw2t2g08.service;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionState;
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
     * Cambia el estado de una pregunta y notifica a las vistas observadoras.
     *
     * @param id       identificador de la pregunta.
     * @param newState estado destino.
     * @return true si el estado se actualizo.
     * @throws IllegalStateException si la transicion no es valida.
     */
    boolean changeState(String id, QuestionState newState);

    /**
     * @return estadisticas actuales del banco de preguntas.
     */
    QuestionStatistics getStatistics();
}
