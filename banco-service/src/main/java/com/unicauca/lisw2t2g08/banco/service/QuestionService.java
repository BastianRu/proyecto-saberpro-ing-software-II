package com.unicauca.lisw2t2g08.banco.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unicauca.lisw2t2g08.banco.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.banco.client.IValidationClient;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.banco.messaging.IQuestionEventPublisher;
import com.unicauca.lisw2t2g08.banco.messaging.QuestionEvent;

/**
 * Logica de negocio central del banco de preguntas.
 *
 * Implementa las reglas de dominio:
 * - Validacion estructural previa con el microservicio validacion-service.
 * - Numeracion consecutiva de preguntas (P-001, P-002, ...).
 * - Restriccion de modificacion unicamente para preguntas en estado Borrador.
 * - Transiciones controladas por la maquina de estados de QuestionState.
 * - Registro de auditoria historica de cada transicion con usuario y fecha.
 * - Publicacion asincrona de eventos a RabbitMQ.
 */
public class QuestionService implements IQuestionService {

    private static final String ID_PREFIX = "P-";

    private final IQuestionRepository repository;
    private final IValidationClient validationClient;
    private final IQuestionEventPublisher eventPublisher;

    public QuestionService(IQuestionRepository repository,
                           IValidationClient validationClient,
                           IQuestionEventPublisher eventPublisher) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio");
        }
        if (validationClient == null) {
            throw new IllegalArgumentException("El cliente de validacion es obligatorio");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("El publicador de eventos es obligatorio");
        }
        this.repository = repository;
        this.validationClient = validationClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Question> listQuestions() {
        return repository.findAll();
    }

    @Override
    public List<Question> searchQuestions(QuestionCriteria criteria) {
        List<Question> all = repository.findAll();
        if (criteria == null) {
            return all;
        }
        return all.stream().filter(criteria::matches).toList();
    }

    @Override
    public Optional<Question> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    @Override
    public Question createQuestion(QuestionDraft draft, String username) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador de la pregunta es obligatorio");
        }

        // Valida con el microservicio de validacion estructural
        validationClient.validate(draft);

        String id = nextId();
        Question question = new Question(
                id,
                draft.getName(),
                draft.getContext(),
                draft.getDirectQuestion(),
                distractorsOf(draft),
                draft.getJustification(),
                draft.getBibliography(),
                draft.getCompetence(),
                draft.getTopic(),
                draft.getSubtopic(),
                draft.getDifficulty(),
                QuestionState.BORRADOR);

        if (!repository.save(question)) {
            throw new IllegalStateException("No fue posible guardar la pregunta " + id);
        }

        // Registro inicial de auditoria
        repository.saveStateChange(new QuestionStateChange(
                id, null, QuestionState.BORRADOR, username, LocalDateTime.now()));

        // Publica evento en RabbitMQ
        eventPublisher.publish(QuestionEvent.created(id, username));

        return question;
    }

    @Override
    public Question updateQuestion(String id, QuestionDraft draft, String username) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador de la pregunta es obligatorio");
        }

        Question question = findById(id)
                .orElseThrow(() -> new IllegalStateException("La pregunta no existe en el banco: " + id));

        // Solo se pueden modificar preguntas en estado Borrador
        if (question.getState() != QuestionState.BORRADOR) {
            throw new IllegalStateException("Solo se pueden modificar preguntas en estado Borrador");
        }

        // Valida con el microservicio
        validationClient.validate(draft);

        question.modifyContent(
                draft.getName(),
                draft.getContext(),
                draft.getDirectQuestion(),
                distractorsOf(draft),
                draft.getJustification(),
                draft.getBibliography(),
                draft.getCompetence(),
                draft.getTopic(),
                draft.getSubtopic(),
                draft.getDifficulty());

        if (!repository.update(question)) {
            throw new IllegalStateException("No fue posible actualizar la pregunta " + id);
        }

        eventPublisher.publish(QuestionEvent.updated(id, username));

        return question;
    }

    @Override
    public boolean changeState(String id, QuestionState newState, String username) {
        if (id == null || id.isBlank()) {
            return false;
        }

        Question question = findById(id)
                .orElseThrow(() -> new IllegalStateException("La pregunta no existe en el banco: " + id));

        QuestionState previous = question.getState();
        if (previous == newState) {
            return true; // Sin cambio efectivo
        }

        // changeState valida con la maquina de estados (QuestionState.canTransitionTo)
        question.changeState(newState);

        boolean updated = repository.update(question);
        if (updated) {
            repository.saveStateChange(new QuestionStateChange(
                    id, previous, newState, username, LocalDateTime.now()));

            eventPublisher.publish(QuestionEvent.stateChanged(id, previous, newState, username));
        }
        return updated;
    }

    @Override
    public List<QuestionStateChange> stateHistory(String id) {
        if (id == null || id.isBlank()) {
            return List.of();
        }
        return repository.findStateHistory(id);
    }

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

    private static QuestionDistractors distractorsOf(QuestionDraft draft) {
        return new QuestionDistractors(draft.getOptions(), draft.getCorrectIndex());
    }

    private synchronized String nextId() {
        int max = 0;
        for (Question question : repository.findAll()) {
            String id = question.getId();
            if (id != null && id.startsWith(ID_PREFIX)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("%s%03d", ID_PREFIX, max + 1);
    }
}
