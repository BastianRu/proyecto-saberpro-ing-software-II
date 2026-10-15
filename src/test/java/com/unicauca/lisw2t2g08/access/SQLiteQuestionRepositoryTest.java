package com.unicauca.lisw2t2g08.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;

/**
 * Pruebas del repositorio SQLite de preguntas sobre una base de datos en
 * memoria, de modo que no se toca el archivo taller2.db.
 */
class SQLiteQuestionRepositoryTest {

    private Connection connection;
    private IQuestionRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        DatabaseConnection.initializeSchema(connection);
        repository = new SQLiteQuestionRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    private Question question(String id) {
        return new Question(id, "Pregunta " + id, "Enunciado de " + id,
                new QuestionDistractors(List.of("Opcion A", "Opcion B", "Opcion C"), 2),
                QuestionState.BORRADOR);
    }

    @Test
    @DisplayName("Guarda una pregunta y la recupera con sus opciones en orden")
    void shouldSaveAndFindQuestionWithOptions() {
        assertTrue(repository.save(question("P-100")));

        Question stored = repository.findById("P-100").orElseThrow();
        assertEquals("Pregunta P-100", stored.getName());
        assertEquals("Enunciado de P-100", stored.getStatement());
        assertEquals(List.of("Opcion A", "Opcion B", "Opcion C"), stored.getDistractors().getOptions());
        assertEquals("Opcion C", stored.getCorrectAnswer());
        assertEquals(QuestionState.BORRADOR, stored.getState());
    }

    @Test
    @DisplayName("Buscar por un id inexistente o nulo devuelve vacio")
    void shouldReturnEmptyForUnknownId() {
        assertTrue(repository.findById("NO-EXISTE").isEmpty());
        assertTrue(repository.findById(null).isEmpty());
    }

    @Test
    @DisplayName("Rechaza ids duplicados y preguntas nulas")
    void shouldSaveOnlyNewQuestions() {
        assertTrue(repository.save(question("P-100")));
        assertFalse(repository.save(question("P-100")));
        assertFalse(repository.save(null));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("Actualiza el estado y las opciones de una pregunta existente")
    void shouldUpdateExistingQuestion() {
        repository.save(question("P-100"));
        Question stored = repository.findById("P-100").orElseThrow();
        stored.changeState(QuestionState.PENDIENTE_REVISION);
        stored.setDistractors(new QuestionDistractors(List.of("Si", "No"), 0));

        assertTrue(repository.update(stored));

        Question updated = repository.findById("P-100").orElseThrow();
        assertEquals(QuestionState.PENDIENTE_REVISION, updated.getState());
        assertEquals(List.of("Si", "No"), updated.getDistractors().getOptions());
        assertEquals("Si", updated.getCorrectAnswer());
    }

    @Test
    @DisplayName("No actualiza preguntas inexistentes ni nulas")
    void shouldNotUpdateUnknownQuestions() {
        assertFalse(repository.update(question("NO-EXISTE")));
        assertFalse(repository.update(null));
        assertTrue(repository.findById("NO-EXISTE").isEmpty());
    }

    @Test
    @DisplayName("Lista todas las preguntas ordenadas por id")
    void shouldListAllQuestions() {
        repository.save(question("P-002"));
        repository.save(question("P-001"));

        List<Question> all = repository.findAll();

        assertEquals(2, all.size());
        assertEquals("P-001", all.get(0).getId());
        assertEquals("P-002", all.get(1).getId());
    }

    @Test
    @DisplayName("Los datos de ejemplo se cargan solo si el repositorio esta vacio")
    void shouldLoadSampleDataOnlyWhenEmpty() {
        QuestionSampleData.loadIfEmpty(repository);
        int sampleSize = repository.findAll().size();
        assertEquals(8, sampleSize);

        repository.findById("P-001").ifPresent(q -> {
            q.changeState(QuestionState.PENDIENTE_REVISION);
            repository.update(q);
        });
        QuestionSampleData.loadIfEmpty(repository);

        assertEquals(sampleSize, repository.findAll().size());
        assertEquals(QuestionState.PENDIENTE_REVISION, repository.findById("P-001").orElseThrow().getState());
    }

    @Test
    @DisplayName("Guarda y actualiza los campos de RF-05")
    void shouldPersistQuestionContentFields() {
        Question question = new Question("P-200", "Nombre", "Contexto", "¿Pregunta?",
                new QuestionDistractors(List.of("Uno", "Dos"), 1), "Justificacion",
                "Bibliografia", "Competencia", "Tema", "Subtema", DifficultyLevel.ALTA,
                QuestionState.BORRADOR);
        repository.save(question);

        Question stored = repository.findById("P-200").orElseThrow();
        assertEquals("Contexto", stored.getContext());
        assertEquals("Justificacion", stored.getJustification());
        assertEquals("Bibliografia", stored.getBibliography());
        assertEquals("Competencia", stored.getCompetence());
        assertEquals("Tema", stored.getTopic());
        assertEquals("Subtema", stored.getSubtopic());
        assertEquals(DifficultyLevel.ALTA, stored.getDifficulty());

        stored.modifyContent("Nombre", "Otro contexto", "¿Pregunta?",
                stored.getDistractors(), "Otra", "Biblio", "Comp", "Tema", "Sub", DifficultyLevel.BAJA);
        assertTrue(repository.update(stored));

        Question updated = repository.findById("P-200").orElseThrow();
        assertEquals("Otro contexto", updated.getContext());
        assertEquals(DifficultyLevel.BAJA, updated.getDifficulty());
    }

    @Test
    @DisplayName("Agrega las columnas nuevas a una base creada antes de RF-05")
    void shouldMigrateOldQuestionsTable() throws SQLException {
        try (Connection old = DriverManager.getConnection("jdbc:sqlite::memory:");
             var stmt = old.createStatement()) {
            stmt.execute("CREATE TABLE questions(id TEXT PRIMARY KEY, name TEXT NOT NULL,"
                    + " statement TEXT NOT NULL, correct_index INTEGER NOT NULL, state TEXT NOT NULL)");
            stmt.execute("INSERT INTO questions VALUES ('P-1', 'Vieja', '¿Pregunta?', 0, 'BORRADOR')");
            DatabaseConnection.initializeSchema(old);
            stmt.execute("INSERT INTO question_options VALUES ('P-1', 0, 'Uno'), ('P-1', 1, 'Dos')");

            Question stored = new SQLiteQuestionRepository(old).findById("P-1").orElseThrow();
            assertEquals("", stored.getContext());
            assertEquals(null, stored.getDifficulty());
        }
    }

    @Test
    @DisplayName("Guarda y consulta el historial de cambios de estado")
    void shouldPersistStateHistory() {
        repository.save(question("P-300"));
        repository.saveStateChange(new QuestionStateChange("P-300", QuestionState.BORRADOR,
                QuestionState.PENDIENTE_REVISION, "juan", LocalDateTime.now()));

        List<QuestionStateChange> history = repository.findStateHistory("P-300");

        assertEquals(1, history.size());
        assertEquals(QuestionState.BORRADOR, history.get(0).getFromState());
        assertEquals(QuestionState.PENDIENTE_REVISION, history.get(0).getToState());
        assertEquals("juan", history.get(0).getUsername());
        assertTrue(repository.findStateHistory("NO-EXISTE").isEmpty());
        assertTrue(repository.findStateHistory(null).isEmpty());
    }
}
