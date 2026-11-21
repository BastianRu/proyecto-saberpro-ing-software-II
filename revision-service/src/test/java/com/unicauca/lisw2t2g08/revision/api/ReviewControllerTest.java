package com.unicauca.lisw2t2g08.revision.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.unicauca.lisw2t2g08.revision.events.QuestionStateMessage;
import com.unicauca.lisw2t2g08.revision.service.BancoRejectedException;
import com.unicauca.lisw2t2g08.revision.service.FakeBancoClient;
import com.unicauca.lisw2t2g08.revision.service.IBancoClient;
import com.unicauca.lisw2t2g08.revision.service.ReviewService;

/** Contrato REST completo, con SQLite en memoria y el Banco reemplazado por un doble. */
@SpringBootTest(properties = {
        "revision.db.url=jdbc:sqlite::memory:",
        "auth.token.secret=secreto-de-prueba-0123456789",
        "spring.rabbitmq.listener.simple.auto-startup=false"})
@AutoConfigureMockMvc
@Import(ReviewControllerTest.TestBanco.class)
class ReviewControllerTest {

    @TestConfiguration
    static class TestBanco {
        @Bean
        @Primary
        FakeBancoClient fakeBanco() {
            return new FakeBancoClient();
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ReviewService service;
    @Autowired
    private FakeBancoClient banco;

    private static int sequence = 0;

    @BeforeEach
    void reset() {
        banco.calls.clear();
        banco.failWith = null;
    }

    private static String bearer(String login, String role) throws Exception {
        String payload = login + "|" + role + "|" + LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secreto-de-prueba-0123456789".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        return "Bearer " + enc.encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "."
                + enc.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    // Crea una revisión pendiente como lo haría el evento del Banco y devuelve su id
    private long pendingReview(String question) {
        service.onQuestionStateChanged(new QuestionStateMessage("ev-" + (++sequence), question,
                "BORRADOR", "PENDIENTE_REVISION", "ana", Instant.now()));
        return service.historyOf(question).get(0).getId();
    }

    @Test
    void sinTokenEs401() throws Exception {
        mvc.perform(get("/revisiones")).andExpect(status().isUnauthorized());
    }

    @Test
    void flujoCompletoTomarYAprobar() throws Exception {
        long id = pendingReview("P-100");

        mvc.perform(get("/revisiones").param("status", "PENDIENTE").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isOk());
        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_CURSO"))
                .andExpect(jsonPath("$.reviewer").value("beto"));
        mvc.perform(post("/revisiones/" + id + "/dictamen").header("Authorization", bearer("beto", "REVISOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"APROBAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETADA"))
                .andExpect(jsonPath("$.decision").value("APROBAR"));

        org.junit.jupiter.api.Assertions.assertEquals("EN_REVISION", banco.calls.get(0).targetState());
        org.junit.jupiter.api.Assertions.assertEquals("APROBADA", banco.calls.get(1).targetState());
    }

    @Test
    void rechazarSinObservacionesEs400() throws Exception {
        long id = pendingReview("P-101");
        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("beto", "REVISOR")));

        mvc.perform(post("/revisiones/" + id + "/dictamen").header("Authorization", bearer("beto", "REVISOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECHAZAR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unDocenteNoPuedeTomarEs403() throws Exception {
        long id = pendingReview("P-102");
        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("doc", "DOCENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAutorNoPuedeRevisarSuPreguntaEs403() throws Exception {
        long id = pendingReview("P-103");
        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("ana", "REVISOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tomarDosVecesEs409() throws Exception {
        long id = pendingReview("P-104");
        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isOk());
        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("carla", "REVISOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void siElBancoRechazaLaTransicionEs409ConSuMensaje() throws Exception {
        long id = pendingReview("P-105");
        banco.failWith = new BancoRejectedException("Transicion no permitida de Borrador a En revisión");

        mvc.perform(post("/revisiones/" + id + "/tomar").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Transicion no permitida de Borrador a En revisión"));
    }

    @Test
    void revisionInexistenteEs404() throws Exception {
        mvc.perform(get("/revisiones/99999").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void historialPorPregunta() throws Exception {
        pendingReview("P-106");
        mvc.perform(get("/revisiones/preguntas/P-106").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].questionId").value("P-106"));
    }

    @Test
    void estadoInvalidoEnElFiltroEs400() throws Exception {
        mvc.perform(get("/revisiones").param("status", "XYZ").header("Authorization", bearer("beto", "REVISOR")))
                .andExpect(status().isBadRequest());
    }
}
