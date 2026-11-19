package com.unicauca.lisw2t2g08.banco.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

import com.unicauca.lisw2t2g08.banco.events.IEventPublisher;
import com.unicauca.lisw2t2g08.banco.service.RecordingEventPublisher;

/** Prueba de punta a punta del contrato REST, con SQLite en memoria y el broker reemplazado por un publicador en memoria. */
@SpringBootTest(properties = {
        "banco.db.url=jdbc:sqlite::memory:",
        "auth.token.secret=secreto-de-prueba-0123456789"})
@AutoConfigureMockMvc
@Import(QuestionControllerTest.TestBroker.class)
class QuestionControllerTest {

    @TestConfiguration
    static class TestBroker {
        @Bean
        @Primary
        RecordingEventPublisher recordingPublisher() {
            return new RecordingEventPublisher();
        }
    }

    private static final String VALID = """
            {"name":"Ciclo del agua",
             "context":"Un estudiante observa cómo el agua de un lago se evapora durante el día.",
             "directQuestion":"¿Qué proceso físico describe mejor lo observado?",
             "options":["Evaporación","Condensación","Solidificación","Sublimación","Fusión"],
             "correctIndex":0,"justification":"La evaporación es el paso de líquido a gas.",
             "bibliography":"Física general","competence":"Ciencias naturales",
             "topic":"Termodinámica","subtopic":"Cambios de fase","difficulty":"MEDIA"}""";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private RecordingEventPublisher publisher;

    // Firma un token igual que lo haría auth-service (mismo formato y secreto)
    private static String tokenFor(String login, String role, LocalDateTime expiresAt) {
        try {
            String payload = login + "|" + role + "|" + expiresAt;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec("secreto-de-prueba-0123456789".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
            return enc.encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "."
                    + enc.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String bearer(String login) {
        return "Bearer " + tokenFor(login, "AUTOR_PREGUNTAS", LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
    }

    private String createQuestion(String login) throws Exception {
        String body = mvc.perform(post("/preguntas").header("Authorization", bearer(login))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("BORRADOR"))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(body, "$.id");
    }

    @Test
    void sinTokenEs401() throws Exception {
        mvc.perform(get("/preguntas")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtroSecretoOVencidoEs401() throws Exception {
        mvc.perform(get("/preguntas").header("Authorization", "Bearer abc.def")).andExpect(status().isUnauthorized());
        String expired = tokenFor("ana", "DOCENTE", LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        mvc.perform(get("/preguntas").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
    }

    @Test
    void crearConsultarYListar() throws Exception {
        String id = createQuestion("ana");

        mvc.perform(get("/preguntas/" + id).header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.directQuestion").value("¿Qué proceso físico describe mejor lo observado?"))
                .andExpect(jsonPath("$.allowedTransitions[0]").value("PENDIENTE_REVISION"));
        mvc.perform(get("/preguntas").param("state", "BORRADOR").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
    }

    @Test
    void borradorInvalidoEs422ConLaListaDeErrores() throws Exception {
        mvc.perform(post("/preguntas").header("Authorization", bearer("ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("Un estudiante observa cómo el agua de un lago se evapora durante el día.", "corto")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void cambiarEstadoPublicaElEventoConElUsuarioDelToken() throws Exception {
        String id = createQuestion("beto");
        publisher.clear();

        mvc.perform(put("/preguntas/" + id + "/estado").header("Authorization", bearer("beto"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\"PENDIENTE_REVISION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PENDIENTE_REVISION"));

        org.junit.jupiter.api.Assertions.assertEquals(1, publisher.stateChanged.size());
        org.junit.jupiter.api.Assertions.assertEquals("beto", publisher.stateChanged.get(0).username());
    }

    @Test
    void transicionInvalidaEs409() throws Exception {
        String id = createQuestion("carla");
        mvc.perform(put("/preguntas/" + id + "/estado").header("Authorization", bearer("carla"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\"PUBLICADA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void estadoDesconocidoEs400YPreguntaInexistenteEs404() throws Exception {
        String id = createQuestion("dani");
        mvc.perform(put("/preguntas/" + id + "/estado").header("Authorization", bearer("dani"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\"XYZ\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/preguntas/P-999").header("Authorization", bearer("dani"))).andExpect(status().isNotFound());
    }

    @Test
    void editarFueraDeBorradorEs409() throws Exception {
        String id = createQuestion("eva");
        mvc.perform(put("/preguntas/" + id + "/estado").header("Authorization", bearer("eva"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"state\":\"PENDIENTE_REVISION\"}"));

        mvc.perform(put("/preguntas/" + id).header("Authorization", bearer("eva"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isConflict());
    }

    @Test
    void estadisticasListanLosSieteEstados() throws Exception {
        createQuestion("fer");
        mvc.perform(get("/preguntas/estadisticas").header("Authorization", bearer("fer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byState.BORRADOR.count").isNumber())
                .andExpect(jsonPath("$.byState.ARCHIVADA.count").value(0))
                .andExpect(jsonPath("$.total").isNumber());
    }
}
