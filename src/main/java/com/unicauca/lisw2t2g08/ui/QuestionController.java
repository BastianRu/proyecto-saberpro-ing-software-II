package com.unicauca.lisw2t2g08.ui;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.service.IQuestionService;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;

import java.util.List;
import java.util.Optional;

/**
 * Capa de presentacion.
 *
 * Controlador del micro patron MVC. Es el unico punto por el que las vistas
 * hablan con el modelo; traduce las acciones del usuario en operaciones del
 * servicio de dominio.
 *
 * SRP: no contiene reglas de negocio ni codigo de dibujo, solo coordina.
 * DIP: depende de la abstraccion IQuestionService, que recibe por constructor.
 */
public class QuestionController {

    private final IQuestionService service;

    /**
     * @param service modelo del MVC (abstraccion del servicio de dominio).
     */
    public QuestionController(IQuestionService service) {
        if (service == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        this.service = service;
    }

    /**
     * @return preguntas para poblar el comboBox de la vista principal.
     */
    public List<Question> listQuestions() {
        return service.listQuestions();
    }

    /**
     * @param id identificador de la pregunta seleccionada.
     * @return la pregunta si existe.
     */
    public Optional<Question> findById(String id) {
        return service.findById(id);
    }

    /**
     * RF-04: solicita crear una pregunta nueva en Borrador.
     */
    public Question createQuestion(QuestionDraft draft) {
        return service.createQuestion(draft);
    }

    /**
     * RF-06: solicita modificar una pregunta en Borrador.
     */
    public Question updateQuestion(String id, QuestionDraft draft) {
        return service.updateQuestion(id, draft);
    }

    /**
     * RF-07: consulta preguntas segun los filtros de la vista.
     */
    public List<Question> searchQuestions(QuestionCriteria criteria) {
        return service.searchQuestions(criteria);
    }

    /**
     * Solicita al modelo el cambio de estado de una pregunta.
     *
     * @param id       identificador de la pregunta.
     * @param newState estado destino.
     * @return true si el estado se actualizo.
     */
    public boolean changeState(String id, QuestionState newState) {
        return service.changeState(id, newState);
    }

    /**
     * @return estadisticas actuales del banco de preguntas.
     */
    public QuestionStatistics getStatistics() {
        return service.getStatistics();
    }
}
