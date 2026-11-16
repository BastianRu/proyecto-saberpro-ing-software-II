package com.unicauca.lisw2t2g08.banco.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.banco.access.SQLiteQuestionRepository;
import com.unicauca.lisw2t2g08.banco.client.IValidationClient;
import com.unicauca.lisw2t2g08.banco.client.ValidationException;
import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.banco.messaging.IQuestionEventPublisher;
import com.unicauca.lisw2t2g08.banco.messaging.QuestionEvent;

import static org.junit.jupiter.api.Assertions.*;

class QuestionServiceTest {

    private Connection connection;
    private SQLiteQuestionRepository repository;
    private TestValidationClient validationClient;
    private TestEventPublisher eventPublisher;
    private QuestionService service;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE questions (" +
                    "id TEXT PRIMARY KEY, name TEXT NOT NULL, statement TEXT NOT NULL, " +
                    "context TEXT, correct_index INTEGER NOT NULL, state TEXT NOT NULL, " +
                    "justification TEXT, bibliography TEXT, competence TEXT, topic TEXT, " +
                    "subtopic TEXT, difficulty TEXT);");

            st.execute("CREATE TABLE question_options (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, question_id TEXT NOT NULL, " +
                    "position INTEGER NOT NULL, text TEXT NOT NULL);");

            st.execute("CREATE TABLE question_state_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, question_id TEXT NOT NULL, " +
                    "from_state TEXT, to_state TEXT NOT NULL, username TEXT NOT NULL, " +
                    "changed_at TEXT NOT NULL);");
        }
        repository = new SQLiteQuestionRepository(connection);
        validationClient = new TestValidationClient();
        eventPublisher = new TestEventPublisher();
        service = new QuestionService(repository, validationClient, eventPublisher);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void createQuestion_validDraft_savesQuestionInBorradorAndPublishesEvent() {
        QuestionDraft draft = sampleDraft();

        Question created = service.createQuestion(draft, "autor1");

        assertNotNull(created);
        assertEquals("P-001", created.getId());
        assertEquals(QuestionState.BORRADOR, created.getState());
        assertEquals("Pregunta de prueba", created.getName());

        assertTrue(validationClient.validated);
        assertEquals(1, eventPublisher.events.size());
        assertEquals("QUESTION_CREATED", eventPublisher.events.get(0).eventType());
        assertEquals("P-001", eventPublisher.events.get(0).questionId());

        List<QuestionStateChange> history = service.stateHistory("P-001");
        assertEquals(1, history.size());
        assertEquals(QuestionState.BORRADOR, history.get(0).getToState());
        assertEquals("autor1", history.get(0).getUsername());
    }

    @Test
    void createQuestion_validationFails_throwsExceptionAndDoesNotPersist() {
        validationClient.throwOnValidate = new ValidationException(List.of("RF-08: contexto muy corto"));
        QuestionDraft draft = sampleDraft();

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.createQuestion(draft, "autor1"));

        assertEquals(1, ex.getErrors().size());
        assertTrue(repository.findAll().isEmpty());
        assertTrue(eventPublisher.events.isEmpty());
    }

    @Test
    void updateQuestion_inBorrador_updatesContent() {
        Question created = service.createQuestion(sampleDraft(), "autor1");

        QuestionDraft modified = sampleDraft();
        modified.setName("Nombre Modificado");
        modified.setDifficulty(DifficultyLevel.ALTA);

        Question updated = service.updateQuestion(created.getId(), modified, "autor1");

        assertEquals("Nombre Modificado", updated.getName());
        assertEquals(DifficultyLevel.ALTA, updated.getDifficulty());
    }

    @Test
    void updateQuestion_notInBorrador_throwsIllegalStateException() {
        Question created = service.createQuestion(sampleDraft(), "autor1");
        service.changeState(created.getId(), QuestionState.PENDIENTE_REVISION, "autor1");

        QuestionDraft modified = sampleDraft();
        modified.setName("Intento de cambio");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.updateQuestion(created.getId(), modified, "autor1"));

        assertTrue(ex.getMessage().contains("Solo se pueden modificar preguntas en estado Borrador"));
    }

    @Test
    void changeState_validTransition_updatesStateAndRecordsAudit() {
        Question created = service.createQuestion(sampleDraft(), "autor1");

        boolean result = service.changeState(created.getId(), QuestionState.PENDIENTE_REVISION, "autor1");
        assertTrue(result);

        Question retrieved = service.findById(created.getId()).orElseThrow();
        assertEquals(QuestionState.PENDIENTE_REVISION, retrieved.getState());

        List<QuestionStateChange> history = service.stateHistory(created.getId());
        assertEquals(2, history.size());
        assertEquals(QuestionState.BORRADOR, history.get(1).getFromState());
        assertEquals(QuestionState.PENDIENTE_REVISION, history.get(1).getToState());
        assertEquals("autor1", history.get(1).getUsername());
    }

    @Test
    void changeState_invalidTransition_throwsIllegalStateException() {
        Question created = service.createQuestion(sampleDraft(), "autor1");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.changeState(created.getId(), QuestionState.APROBADA, "revisor1"));

        assertTrue(ex.getMessage().contains("Transición no permitida"));
    }

    @Test
    void searchQuestions_byCriteria() {
        Question q1 = service.createQuestion(sampleDraft(), "autor1");
        service.changeState(q1.getId(), QuestionState.PENDIENTE_REVISION, "autor1");

        QuestionDraft draft2 = sampleDraft();
        draft2.setName("Pregunta de Bases de Datos");
        draft2.setCompetence("Bases de Datos");
        draft2.setDifficulty(DifficultyLevel.ALTA);
        service.createQuestion(draft2, "autor2");

        QuestionCriteria criteria = new QuestionCriteria()
                .withDifficulty(DifficultyLevel.ALTA);

        List<Question> results = service.searchQuestions(criteria);
        assertEquals(1, results.size());
        assertEquals("Pregunta de Bases de Datos", results.get(0).getName());
    }

    @Test
    void getStatistics_calculatesCountsAndPercentages() {
        Question q1 = service.createQuestion(sampleDraft(), "autor1");
        service.changeState(q1.getId(), QuestionState.PENDIENTE_REVISION, "autor1");
        service.createQuestion(sampleDraft(), "autor2");

        QuestionStatistics stats = service.getStatistics();
        assertEquals(2, stats.getTotal());
        assertEquals(1, stats.getCount(QuestionState.BORRADOR));
        assertEquals(1, stats.getCount(QuestionState.PENDIENTE_REVISION));
        assertEquals(50.0, stats.getPercentage(QuestionState.BORRADOR));
    }

    private QuestionDraft sampleDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setName("Pregunta de prueba");
        draft.setContext("Este es un contexto valido de mas de 20 caracteres.");
        draft.setDirectQuestion("Cual es la respuesta correcta?");
        draft.setOptions(List.of("Opcion A", "Opcion B", "Opcion C", "Opcion D", "Opcion E"));
        draft.setCorrectIndex(0);
        draft.setJustification("Justificacion pedagogica completa.");
        draft.setBibliography("Bibliografia del curso.");
        draft.setCompetence("Ingenieria de Software");
        draft.setTopic("Arquitectura");
        draft.setSubtopic("Microservicios");
        draft.setDifficulty(DifficultyLevel.MEDIA);
        return draft;
    }

    private static class TestValidationClient implements IValidationClient {
        boolean validated = false;
        ValidationException throwOnValidate = null;

        @Override
        public void validate(QuestionDraft draft) {
            validated = true;
            if (throwOnValidate != null) {
                throw throwOnValidate;
            }
        }
    }

    private static class TestEventPublisher implements IQuestionEventPublisher {
        final List<QuestionEvent> events = new ArrayList<>();

        @Override
        public void publish(QuestionEvent event) {
            events.add(event);
        }
    }
}
