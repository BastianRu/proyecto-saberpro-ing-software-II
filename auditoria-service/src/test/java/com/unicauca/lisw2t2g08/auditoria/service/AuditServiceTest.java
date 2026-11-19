package com.unicauca.lisw2t2g08.auditoria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.auditoria.access.SQLiteAuditRepository;
import com.unicauca.lisw2t2g08.auditoria.config.AuditConfig;
import com.unicauca.lisw2t2g08.auditoria.events.QuestionEventMessage;

/** Se prueba contra SQLite real en memoria: la idempotencia depende de la restricción UNIQUE de la base. */
class AuditServiceTest {

    private Connection connection;
    private AuditService service;

    @BeforeEach
    void setUp() throws Exception {
        connection = new AuditConfig().connection("jdbc:sqlite::memory:");
        service = new AuditService(new SQLiteAuditRepository(connection));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private static QuestionEventMessage stateChanged(String eventId, String from, String to, String at) {
        return new QuestionEventMessage(eventId, "P-001", from, to, "ana", Instant.parse(at));
    }

    @Test
    void registraUnaCreacionConSuTipoYSinEstados() {
        assertTrue(service.record("question.created",
                new QuestionEventMessage("e1", "P-001", null, null, "ana", Instant.parse("2026-11-19T10:00:00Z"))));

        var history = service.history("P-001");
        assertEquals(1, history.size());
        assertEquals("QuestionCreated", history.get(0).eventType());
        assertEquals("ana", history.get(0).username());
    }

    @Test
    void elMismoEventoEntregadoDosVecesSeRegistraUnaSolaVez() {
        QuestionEventMessage message = stateChanged("e1", "BORRADOR", "PENDIENTE_REVISION", "2026-11-19T10:00:00Z");

        assertTrue(service.record("question.state-changed", message));
        assertFalse(service.record("question.state-changed", message));

        assertEquals(1, service.history("P-001").size());
    }

    @Test
    void elHistorialSeDevuelveEnOrdenCronologicoAunqueLleguenDesordenados() {
        service.record("question.state-changed", stateChanged("e2", "PENDIENTE_REVISION", "EN_REVISION", "2026-11-19T11:00:00Z"));
        service.record("question.state-changed", stateChanged("e1", "BORRADOR", "PENDIENTE_REVISION", "2026-11-19T10:00:00Z"));

        var history = service.history("P-001");

        assertEquals("PENDIENTE_REVISION", history.get(0).newState());
        assertEquals("EN_REVISION", history.get(1).newState());
    }

    @Test
    void claveDeEnrutamientoDesconocidaSeIgnora() {
        assertFalse(service.record("otra.cosa", stateChanged("e1", "A", "B", "2026-11-19T10:00:00Z")));
        assertTrue(service.history("P-001").isEmpty());
    }

    @Test
    void elHistorialSoloIncluyeLaPreguntaPedida() {
        service.record("question.created", new QuestionEventMessage("e1", "P-001", null, null, "ana", Instant.now()));
        service.record("question.created", new QuestionEventMessage("e2", "P-002", null, null, "ana", Instant.now()));

        assertEquals(1, service.history("P-002").size());
        assertTrue(service.history("P-999").isEmpty());
    }
}
