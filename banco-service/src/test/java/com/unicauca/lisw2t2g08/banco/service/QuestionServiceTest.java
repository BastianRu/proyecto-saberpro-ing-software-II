package com.unicauca.lisw2t2g08.banco.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.InvalidStateOperationException;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;
import com.unicauca.lisw2t2g08.banco.pipeline.QuestionPipeline;
import com.unicauca.lisw2t2g08.banco.pipeline.ValidationException;

class QuestionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:00:00Z");

    private FakeQuestionRepository repository;
    private RecordingEventPublisher events;
    private QuestionService service;

    @BeforeEach
    void setUp() {
        repository = new FakeQuestionRepository();
        events = new RecordingEventPublisher();
        service = new QuestionService(repository, new QuestionPipeline(), events,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static QuestionDraft validDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setName("Ciclo del agua");
        draft.setContext("Un estudiante observa cómo el agua de un lago se evapora durante el día.");
        draft.setDirectQuestion("¿Qué proceso físico describe mejor lo observado?");
        draft.setOptions(List.of("Evaporación", "Condensación", "Solidificación", "Sublimación", "Fusión"));
        draft.setCorrectIndex(0);
        draft.setJustification("La evaporación es el paso de líquido a gas.");
        draft.setBibliography("Física general, cap. 4");
        draft.setCompetence("Ciencias naturales");
        draft.setTopic("Termodinámica");
        draft.setSubtopic("Cambios de fase");
        draft.setDifficulty(DifficultyLevel.MEDIA);
        return draft;
    }

    @Test
    void crearGuardaEnBorradorYPublicaQuestionCreated() {
        Question question = service.createQuestion(validDraft(), "ana");

        assertEquals("P-001", question.getId());
        assertSame(QuestionStates.BORRADOR, question.getState());
        assertEquals(1, events.created.size());
        assertEquals("P-001", events.created.get(0).questionId());
        assertEquals("ana", events.created.get(0).username());
        assertEquals(NOW, events.created.get(0).occurredAt());
        assertTrue(events.created.get(0).eventId() != null && !events.created.get(0).eventId().isBlank());
    }

    @Test
    void ids_sonConsecutivos() {
        service.createQuestion(validDraft(), "ana");
        assertEquals("P-002", service.createQuestion(validDraft(), "ana").getId());
    }

    @Test
    void borradorInvalidoNoSeGuardaNiPublicaEvento() {
        QuestionDraft draft = validDraft();
        draft.setContext("corto");

        assertThrows(ValidationException.class, () -> service.createQuestion(draft, "ana"));

        assertTrue(repository.findAll().isEmpty());
        assertTrue(events.created.isEmpty());
    }

    @Test
    void cambiarEstadoPersistePublicaEventoConEstadosPreviosYNuevos() {
        Question question = service.createQuestion(validDraft(), "ana");
        events.clear();

        service.changeState(question.getId(), "PENDIENTE_REVISION", "ana");

        assertSame(QuestionStates.PENDIENTE_REVISION, repository.findById("P-001").orElseThrow().getState());
        assertEquals(1, events.stateChanged.size());
        var event = events.stateChanged.get(0);
        assertEquals("BORRADOR", event.previousState());
        assertEquals("PENDIENTE_REVISION", event.newState());
        assertEquals("ana", event.username());
    }

    @Test
    void transicionInvalidaLanzaExcepcionYNoPublicaNiPersiste() {
        Question question = service.createQuestion(validDraft(), "ana");
        events.clear();

        assertThrows(InvalidStateOperationException.class,
                () -> service.changeState(question.getId(), "PUBLICADA", "ana"));

        assertSame(QuestionStates.BORRADOR, repository.findById("P-001").orElseThrow().getState());
        assertTrue(events.stateChanged.isEmpty());
    }

    @Test
    void cambiarAlMismoEstadoNoPublicaEvento() {
        service.createQuestion(validDraft(), "ana");
        events.clear();

        service.changeState("P-001", "BORRADOR", "ana");

        assertTrue(events.stateChanged.isEmpty());
    }

    @Test
    void estadoDesconocidoEsArgumentoInvalido() {
        service.createQuestion(validDraft(), "ana");
        assertThrows(IllegalArgumentException.class, () -> service.changeState("P-001", "XYZ", "ana"));
    }

    @Test
    void preguntaInexistenteLanzaNotFound() {
        assertThrows(QuestionNotFoundException.class, () -> service.changeState("P-999", "BORRADOR", "ana"));
        assertThrows(QuestionNotFoundException.class, () -> service.getQuestion("P-999"));
    }

    @Test
    void siElBrokerFallaElCambioQuedaGuardadoYNoSePropagaElError() {
        service.createQuestion(validDraft(), "ana");
        events.failing = true;

        service.changeState("P-001", "PENDIENTE_REVISION", "ana");

        assertSame(QuestionStates.PENDIENTE_REVISION, repository.findById("P-001").orElseThrow().getState());
    }

    @Test
    void soloSePuedeEditarEnBorradorYSeRevalida() {
        service.createQuestion(validDraft(), "ana");
        QuestionDraft edited = validDraft();
        edited.setName("Nombre nuevo");
        assertEquals("Nombre nuevo", service.updateQuestion("P-001", edited).getName());

        QuestionDraft invalid = validDraft();
        invalid.setContext("corto");
        assertThrows(ValidationException.class, () -> service.updateQuestion("P-001", invalid));

        service.changeState("P-001", "PENDIENTE_REVISION", "ana");
        assertThrows(InvalidStateOperationException.class, () -> service.updateQuestion("P-001", validDraft()));
    }

    @Test
    void estadisticasContabilizanPorEstadoIncluyendoLosVacios() {
        service.createQuestion(validDraft(), "ana");
        service.createQuestion(validDraft(), "ana");
        service.changeState("P-002", "PENDIENTE_REVISION", "ana");

        var stats = service.getStatistics();

        assertEquals(2, stats.getTotal());
        assertEquals(1, stats.getCount(QuestionStates.BORRADOR));
        assertEquals(1, stats.getCount(QuestionStates.PENDIENTE_REVISION));
        assertEquals(0, stats.getCount(QuestionStates.PUBLICADA));
        assertEquals(50.0, stats.getPercentage(QuestionStates.BORRADOR));
    }

    @Test
    void buscarAplicaLosCriterios() {
        service.createQuestion(validDraft(), "ana");
        service.createQuestion(validDraft(), "ana");
        service.changeState("P-002", "PENDIENTE_REVISION", "ana");

        List<Question> found = service.searchQuestions(
                new QuestionCriteria().withState(QuestionStates.PENDIENTE_REVISION));

        assertEquals(1, found.size());
        assertNotEquals("P-001", found.get(0).getId());
        assertEquals(2, service.searchQuestions(null).size());
    }
}
