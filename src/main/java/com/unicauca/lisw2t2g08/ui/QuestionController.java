package com.unicauca.lisw2t2g08.ui;

import com.unicauca.lisw2t2g08.domain.Question;
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
