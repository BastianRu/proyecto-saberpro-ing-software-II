package com.unicauca.lisw2t2g08.banco.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteQuestionRepositoryTest {

    private Connection connection;
    private SQLiteQuestionRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE questions (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "statement TEXT NOT NULL, " +
                    "context TEXT, " +
                    "correct_index INTEGER NOT NULL, " +
                    "state TEXT NOT NULL, " +
                    "justification TEXT, " +
                    "bibliography TEXT, " +
                    "competence TEXT, " +
                    "topic TEXT, " +
                    "subtopic TEXT, " +
                    "difficulty TEXT);");

            st.execute("CREATE TABLE question_options (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question_id TEXT NOT NULL, " +
                    "position INTEGER NOT NULL, " +
                    "text TEXT NOT NULL);");

            st.execute("CREATE TABLE question_state_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question_id TEXT NOT NULL, " +
                    "from_state TEXT, " +
                    "to_state TEXT NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "changed_at TEXT NOT NULL);");
        }
        repository = new SQLiteQuestionRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void saveAndFindById_persistsQuestionWithOptions() {
        Question question = sampleQuestion("P-001", "Pregunta 1", QuestionState.BORRADOR);

        boolean saved = repository.save(question);
        assertTrue(saved);

        Optional<Question> found = repository.findById("P-001");
        assertTrue(found.isPresent());
        assertEquals("P-001", found.get().getId());
        assertEquals("Pregunta 1", found.get().getName());
        assertEquals(5, found.get().getDistractors().getOptions().size());
        assertEquals(0, found.get().getDistractors().getCorrectIndex());
        assertEquals("Opcion A", found.get().getCorrectAnswer());
    }

    @Test
    void update_modifiesFieldsAndOptions() {
        Question question = sampleQuestion("P-001", "Nombre inicial", QuestionState.BORRADOR);
        repository.save(question);

        question.modifyContent(
                "Nombre actualizado",
                "Contexto nuevo de prueba mas largo",
                "Enunciado nuevo?",
                new QuestionDistractors(List.of("A1", "B2", "C3", "D4", "E5"), 2),
                "Nueva justificacion",
                "Nueva biblio",
                "Comp2",
                "Topic2",
                "Sub2",
                DifficultyLevel.ALTA);

        boolean updated = repository.update(question);
        assertTrue(updated);

        Question retrieved = repository.findById("P-001").orElseThrow();
        assertEquals("Nombre actualizado", retrieved.getName());
        assertEquals(DifficultyLevel.ALTA, retrieved.getDifficulty());
        assertEquals(2, retrieved.getDistractors().getCorrectIndex());
        assertEquals("C3", retrieved.getCorrectAnswer());
    }

    @Test
    void saveStateChangeAndFindHistory_persistsAuditTrail() {
        QuestionStateChange change1 = new QuestionStateChange(
                "P-001", null, QuestionState.BORRADOR, "autor1", LocalDateTime.now());
        QuestionStateChange change2 = new QuestionStateChange(
                "P-001", QuestionState.BORRADOR, QuestionState.PENDIENTE_REVISION, "autor1", LocalDateTime.now().plusMinutes(1));

        repository.saveStateChange(change1);
        repository.saveStateChange(change2);

        List<QuestionStateChange> history = repository.findStateHistory("P-001");
        assertEquals(2, history.size());
        assertNull(history.get(0).getFromState());
        assertEquals(QuestionState.BORRADOR, history.get(0).getToState());
        assertEquals("autor1", history.get(0).getUsername());

        assertEquals(QuestionState.BORRADOR, history.get(1).getFromState());
        assertEquals(QuestionState.PENDIENTE_REVISION, history.get(1).getToState());
    }

    private Question sampleQuestion(String id, String name, QuestionState state) {
        return new Question(
                id,
                name,
                "Contexto suficientemente largo para pruebas unitarias",
                "Cual es la respuesta correcta?",
                new QuestionDistractors(List.of("Opcion A", "Opcion B", "Opcion C", "Opcion D", "Opcion E"), 0),
                "Justificacion de ejemplo",
                "Bibliografia de ejemplo",
                "Ingenieria de Software",
                "Arquitectura",
                "Capas",
                DifficultyLevel.MEDIA,
                state);
    }
}
