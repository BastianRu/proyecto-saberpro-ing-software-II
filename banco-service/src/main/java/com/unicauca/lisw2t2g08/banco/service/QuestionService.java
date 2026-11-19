package com.unicauca.lisw2t2g08.banco.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unicauca.lisw2t2g08.banco.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.banco.domain.InvalidStateOperationException;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.banco.events.IEventPublisher;
import com.unicauca.lisw2t2g08.banco.events.QuestionCreatedEvent;
import com.unicauca.lisw2t2g08.banco.events.QuestionStateChangedEvent;
import com.unicauca.lisw2t2g08.banco.pipeline.QuestionPipeline;

/**
 * Lógica de negocio del Banco. La validación estructural (HU03) corre en el mismo
 * proceso: es la tubería de filtros, ya no un servicio aparte. El Observer del monolito
 * desaparece: los cambios se comunican con eventos hacia otros servicios.
 */
public class QuestionService implements IQuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final String ID_PREFIX = "P-";

    private final IQuestionRepository repository;
    private final QuestionPipeline pipeline;
    private final IEventPublisher events;
    private final Clock clock;

    public QuestionService(IQuestionRepository repository, QuestionPipeline pipeline,
                           IEventPublisher events, Clock clock) {
        this.repository = repository;
        this.pipeline = pipeline;
        this.events = events;
        this.clock = clock;
    }

    // synchronized: nextId() lee y save() escribe; sin esto dos altas simultáneas podrían obtener el mismo id
    @Override
    public synchronized Question createQuestion(QuestionDraft draft, String username) {
        requireDraft(draft);
        pipeline.run(draft);
        Question question = new Question(nextId(), draft.getName(), draft.getContext(),
                draft.getDirectQuestion(), distractorsOf(draft), draft.getJustification(),
                draft.getBibliography(), draft.getCompetence(), draft.getTopic(),
                draft.getSubtopic(), draft.getDifficulty(), QuestionStates.BORRADOR);
        if (!repository.save(question)) {
            throw new IllegalStateException("No fue posible guardar la pregunta " + question.getId());
        }
        publishSafely(() -> events.publish(new QuestionCreatedEvent(
                newEventId(), question.getId(), usernameOrSystem(username), Instant.now(clock))));
        return question;
    }

    @Override
    public Question updateQuestion(String id, QuestionDraft draft) {
        requireDraft(draft);
        Question question = getQuestion(id);
        // Se comprueba el estado antes de validar para no pedir correcciones inútiles
        if (!question.getState().isEditable()) {
            throw new InvalidStateOperationException(
                    "Solo se pueden modificar preguntas en estado Borrador");
        }
        pipeline.run(draft);
        question.modifyContent(draft.getName(), draft.getContext(), draft.getDirectQuestion(),
                distractorsOf(draft), draft.getJustification(), draft.getBibliography(),
                draft.getCompetence(), draft.getTopic(), draft.getSubtopic(), draft.getDifficulty());
        if (!repository.update(question)) {
            throw new IllegalStateException("No fue posible actualizar la pregunta " + id);
        }
        return question;
    }

    @Override
    public Question getQuestion(String id) {
        if (id == null || id.isBlank()) {
            throw new QuestionNotFoundException(id);
        }
        return repository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));
    }

    @Override
    public List<Question> searchQuestions(QuestionCriteria criteria) {
        List<Question> all = repository.findAll();
        return criteria == null ? all : all.stream().filter(criteria::matches).toList();
    }

    @Override
    public Question changeState(String id, String targetState, String username) {
        Question question = getQuestion(id);
        QuestionState target = QuestionStates.fromCode(targetState);
        QuestionState previous = question.getState();

        question.changeState(target); // el patrón State decide si es legal
        if (previous == question.getState()) {
            return question; // sin cambio real: no se persiste ni se publica
        }
        if (!repository.update(question)) {
            throw new IllegalStateException("No fue posible actualizar la pregunta " + id);
        }
        publishSafely(() -> events.publish(new QuestionStateChangedEvent(
                newEventId(), id, previous.getCode(), question.getState().getCode(),
                usernameOrSystem(username), Instant.now(clock))));
        return question;
    }

    @Override
    public QuestionStatistics getStatistics() {
        Map<String, Integer> counts = repository.countByState();
        return new QuestionStatistics(counts);
    }

    // El cambio ya está guardado: si el broker falla no se revierte, solo se deja constancia.
    // Limitación conocida: ese evento se pierde (la solución completa sería un outbox transaccional).
    private void publishSafely(Runnable publication) {
        try {
            publication.run();
        } catch (RuntimeException e) {
            log.warn("No se pudo publicar el evento; los consumidores no se enterarán de este cambio", e);
        }
    }

    private static void requireDraft(QuestionDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador de la pregunta es obligatorio");
        }
    }

    // Las opciones del borrador ya incluyen la correcta; la tubería validó su índice
    private static QuestionDistractors distractorsOf(QuestionDraft draft) {
        return new QuestionDistractors(draft.getOptions(), draft.getCorrectIndex());
    }

    private static String usernameOrSystem(String username) {
        return (username == null || username.isBlank()) ? "sistema" : username;
    }

    private static String newEventId() {
        return UUID.randomUUID().toString();
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
}
