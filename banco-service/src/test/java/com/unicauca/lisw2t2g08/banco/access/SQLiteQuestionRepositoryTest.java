package com.unicauca.lisw2t2g08.banco.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.banco.config.BancoConfig;
import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;

class SQLiteQuestionRepositoryTest {

    private Connection connection;
    private SQLiteQuestionRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        // Se usa el mismo bean de configuración que arma el esquema en producción
        connection = new BancoConfig().connection("jdbc:sqlite::memory:");
        repository = new SQLiteQuestionRepository(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private static Question question(String id) {
        return new Question(id, "Nombre", "Contexto de prueba", "¿Pregunta?",
                new QuestionDistractors(List.of("Uno", "Dos", "Tres", "Cuatro", "Cinco"), 2),
                "Justificación", "Bibliografía", "Competencia", "Tema", "Subtema",
                DifficultyLevel.ALTA, QuestionStates.BORRADOR);
    }

    @Test
    void guardaYRecuperaTodosLosCamposIncluidoElEstadoComoObjeto() {
        assertTrue(repository.save(question("P-001")));

        Question loaded = repository.findById("P-001").orElseThrow();

        assertEquals(5, loaded.getDistractors().getOptions().size());
        assertEquals(2, loaded.getDistractors().getCorrectIndex());
        assertEquals(DifficultyLevel.ALTA, loaded.getDifficulty());
        assertSame(QuestionStates.BORRADOR, loaded.getState());
    }

    @Test
    void noGuardaDosVecesElMismoId() {
        assertTrue(repository.save(question("P-001")));
        assertFalse(repository.save(question("P-001")));
    }

    @Test
    void actualizaElEstadoPersistidoComoCodigo() throws Exception {
        repository.save(question("P-001"));
        Question q = repository.findById("P-001").orElseThrow();
        q.submitForReview();

        assertTrue(repository.update(q));

        assertSame(QuestionStates.PENDIENTE_REVISION, repository.findById("P-001").orElseThrow().getState());
        try (Statement st = connection.createStatement();
             var rs = st.executeQuery("SELECT state FROM questions WHERE id='P-001'")) {
            assertEquals("PENDIENTE_REVISION", rs.getString(1));
        }
    }

    @Test
    void cuentaPorEstado() {
        repository.save(question("P-001"));
        repository.save(question("P-002"));
        Question q = repository.findById("P-002").orElseThrow();
        q.submitForReview();
        repository.update(q);

        var counts = repository.countByState();

        assertEquals(1, counts.get("BORRADOR"));
        assertEquals(1, counts.get("PENDIENTE_REVISION"));
        assertFalse(counts.containsKey("PUBLICADA"));
    }
}
