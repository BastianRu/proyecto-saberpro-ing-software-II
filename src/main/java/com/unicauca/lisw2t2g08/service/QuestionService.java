package com.unicauca.lisw2t2g08.service;

import com.unicauca.lisw2t2g08.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.infra.Subject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Capa de servicio.
 *
 * Servicio de la entidad Question y modelo del micro patron MVC. Concentra la
 * logica de negocio y, al extender Subject, actua como sujeto observable: cada
 * vez que cambia el estado de una pregunta notifica a las vistas registradas.
 *
 * DIP: recibe el repositorio por constructor a traves de su abstraccion y
 * publica sus operaciones de negocio mediante la interfaz IQuestionService.
 * SRP: no dibuja interfaces ni sabe como se persisten los datos.
 */
public class QuestionService extends Subject implements IQuestionService {

    private final IQuestionRepository repository;

    /**
     * @param repository implementacion de la persistencia de preguntas.
     */
    public QuestionService(IQuestionRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio");
        }
        this.repository = repository;
    }

    /**
     * @return todas las preguntas del banco.
     */
    @Override
    public List<Question> listQuestions() {
        return repository.findAll();
    }

    /**
     * @param id identificador de la pregunta.
     * @return la pregunta si existe.
     */
    @Override
    public Optional<Question> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    /**
     * Registra una pregunta nueva y notifica a los observadores.
     *
     * @param question pregunta a registrar.
     * @return true si se registro.
     */
    @Override
    public boolean registerQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("La pregunta es obligatoria");
        }
        boolean saved = repository.save(question);
        if (saved) {
            notifyAllObservers(getStatistics());
        }
        return saved;
    }

    /**
     * Cambia el estado de una pregunta y, si el cambio se aplica, notifica a
     * todas las vistas observadoras con las estadisticas actualizadas.
     *
     * @param id       identificador de la pregunta.
     * @param newState estado destino.
     * @return true si el estado se actualizo.
     * @throws IllegalStateException si la transicion no es valida.
     */
    @Override
    public boolean changeState(String id, QuestionState newState) {
        Optional<Question> found = findById(id);
        if (found.isEmpty()) {
            return false;
        }
        Question question = found.get();
        question.changeState(newState);
        boolean updated = repository.update(question);
        if (updated) {
            notifyAllObservers(getStatistics());
        }
        return updated;
    }

    /**
     * Calcula cuantas preguntas hay en cada estado.
     *
     * @return estadisticas del banco de preguntas.
     */
    @Override
    public QuestionStatistics getStatistics() {
        Map<QuestionState, Integer> counts = new EnumMap<>(QuestionState.class);
        for (QuestionState state : QuestionState.values()) {
            counts.put(state, 0);
        }
        for (Question question : repository.findAll()) {
            counts.merge(question.getState(), 1, Integer::sum);
        }
        return new QuestionStatistics(counts);
    }
}
