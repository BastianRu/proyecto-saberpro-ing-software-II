package com.unicauca.lisw2t2g08.banco.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.banco.config.BancoConfig;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;

class QuestionSampleDataTest {

    @Test
    void cargaLosOchoEjemplosEnTodosLosEstadosYSoloSiLaBaseEstaVacia() throws Exception {
        try (Connection connection = new BancoConfig().connection("jdbc:sqlite::memory:")) {
            SQLiteQuestionRepository repository = new SQLiteQuestionRepository(connection);

            QuestionSampleData.loadIfEmpty(repository);
            QuestionSampleData.loadIfEmpty(repository); // segunda llamada: no duplica

            assertEquals(8, repository.findAll().size());
            assertTrue(repository.findAll().stream().map(q -> q.getState()).distinct().count() >= 6);
            assertEquals(QuestionStates.BORRADOR, repository.findById("P-001").orElseThrow().getState());
        }
    }
}
