package com.unicauca.lisw2t2g08.service;

import com.unicauca.lisw2t2g08.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.infra.Subject;
import com.unicauca.lisw2t2g08.pipeline.QuestionPipeline;

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

    private static final String ID_PREFIX = "P-";

    private final IQuestionRepository repository;
    private final QuestionPipeline pipeline;

    /**
     * @param repository implementacion de la persistencia de preguntas.
     */
    public QuestionService(IQuestionRepository repository) {
        this(repository, new QuestionPipeline());
    }

    /**
     * @param repository implementacion de la persistencia de preguntas.
     * @param pipeline   tuberia de validacion estructural (HU03).
     */
    public QuestionService(IQuestionRepository repository, QuestionPipeline pipeline) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio");
        }
        if (pipeline == null) {
            throw new IllegalArgumentException("La tuberia de validacion es obligatoria");
        }
        this.repository = repository;
        this.pipeline = pipeline;
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

    @Override
    public Question createQuestion(QuestionDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador de la pregunta es obligatorio");
        }
        pipeline.run(draft);
        Question question = new Question(nextId(), draft.getName(), draft.getContext(),
                draft.getDirectQuestion(), distractorsOf(draft), draft.getJustification(),
                draft.getBibliography(), draft.getCompetence(), draft.getTopic(),
                draft.getSubtopic(), draft.getDifficulty(), QuestionState.BORRADOR);
        if (!repository.save(question)) {
            throw new IllegalStateException("No fue posible guardar la pregunta " + question.getId());
        }
        notifyAllObservers(getStatistics());
        return question;
    }

    @Override
    public Question updateQuestion(String id, QuestionDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador de la pregunta es obligatorio");
        }
        Question question = findById(id)
                .orElseThrow(() -> new IllegalStateException("La pregunta no existe en el banco"));
        // Se comprueba el estado antes de validar para no pedir correcciones inutiles
        if (question.getState() != QuestionState.BORRADOR) {
            throw new IllegalStateException("Solo se pueden modificar preguntas en estado Borrador");
        }
        pipeline.run(draft);
        question.modifyContent(draft.getName(), draft.getContext(), draft.getDirectQuestion(),
                distractorsOf(draft), draft.getJustification(), draft.getBibliography(),
                draft.getCompetence(), draft.getTopic(), draft.getSubtopic(), draft.getDifficulty());
        if (!repository.update(question)) {
            throw new IllegalStateException("No fue posible actualizar la pregunta " + id);
        }
        notifyAllObservers(getStatistics());
        return question;
    }

    @Override
    public List<Question> searchQuestions(QuestionCriteria criteria) {
        List<Question> all = repository.findAll();
        if (criteria == null) {
            return all;
        }
        return all.stream().filter(criteria::matches).toList();
    }

    // Las opciones del borrador ya incluyen la correcta; la tuberia valido su indice
    private static QuestionDistractors distractorsOf(QuestionDraft draft) {
        return new QuestionDistractors(draft.getOptions(), draft.getCorrectIndex());
    }

    // Siguiente id consecutivo con el formato "P-001"
    private String nextId() {
        int max = 0;
        for (Question question : repository.findAll()) {
            String id = question.getId();
            if (id.startsWith(ID_PREFIX)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    // ids con otro formato no cuentan para la secuencia
                }
            }
        }
        return String.format("%s%03d", ID_PREFIX, max + 1);
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
