package com.unicauca.lisw2t2g08.auditoria.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.unicauca.lisw2t2g08.auditoria.events.QuestionEventMessage;
import com.unicauca.lisw2t2g08.auditoria.service.AuditService;

@SpringBootTest(properties = {
        "auditoria.db.url=jdbc:sqlite::memory:",
        "auth.token.secret=secreto-de-prueba-0123456789",
        "spring.rabbitmq.listener.simple.auto-startup=false"})
@AutoConfigureMockMvc
class AuditControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AuditService service;

    private static String bearer() throws Exception {
        String payload = "ana|REVISOR|" + LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secreto-de-prueba-0123456789".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        return "Bearer " + enc.encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "."
                + enc.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void sinTokenEs401() throws Exception {
        mvc.perform(get("/auditoria/preguntas/P-001")).andExpect(status().isUnauthorized());
    }

    @Test
    void devuelveElHistorialDeLaPregunta() throws Exception {
        service.record("question.created", new QuestionEventMessage("c1", "P-777", null, null, "beto",
                Instant.parse("2026-11-19T10:00:00Z")));
        service.record("question.state-changed", new QuestionEventMessage("c2", "P-777", "BORRADOR",
                "PENDIENTE_REVISION", "beto", Instant.parse("2026-11-19T10:05:00Z")));

        mvc.perform(get("/auditoria/preguntas/P-777").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventType").value("QuestionCreated"))
                .andExpect(jsonPath("$[1].previousState").value("BORRADOR"))
                .andExpect(jsonPath("$[1].newState").value("PENDIENTE_REVISION"))
                .andExpect(jsonPath("$[1].username").value("beto"));
    }

    @Test
    void preguntaSinRegistrosDevuelveListaVacia() throws Exception {
        mvc.perform(get("/auditoria/preguntas/P-000").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
