package com.unicauca.lisw2t2g08.banco.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.unicauca.lisw2t2g08.banco.client.ValidationException;
import com.unicauca.lisw2t2g08.banco.client.ValidationServiceUnavailableException;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.banco.domain.Role;
import com.unicauca.lisw2t2g08.banco.security.TokenVerifier;
import com.unicauca.lisw2t2g08.banco.service.IQuestionService;

/**
 * Verifica la traduccion HTTP de las excepciones del banco sin levantar la
 * aplicacion completa ni requerir base de datos o RabbitMQ.
 */
class ApiExceptionHandlerTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private FakeQuestionService questionService;
    private MockMvc mvc;
    private String token;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneId.of("UTC"));
        questionService = new FakeQuestionService();
        mvc = MockMvcBuilders
                .standaloneSetup(new QuestionController(questionService, new TokenVerifier(SECRET, clock)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        token = "Bearer " + createToken("ana", Role.ADMINISTRADOR);
    }

    @Test
    void validacionNoDisponibleResponde503() throws Exception {
        questionService.createFailure = new ValidationServiceUnavailableException(
                "No fue posible comunicarse con el servicio de validacion");

        mvc.perform(post("/preguntas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value(
                        "No fue posible comunicarse con el servicio de validacion"));
    }

    @Test
    void reglasDeValidacionSiguenRespondiendo422() throws Exception {
        questionService.createFailure = new ValidationException(
                List.of("RF-08: el contexto es obligatorio"));

        mvc.perform(post("/preguntas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("RF-08: el contexto es obligatorio"));
    }

    private String createToken(String subject, Role role) {
        LocalDateTime future = LocalDateTime.parse("2026-11-04T13:00");
        String payload = subject + "|" + role.name() + "|" + future;
        String encodedPayload = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeQuestionService implements IQuestionService {

        private RuntimeException createFailure;

        @Override
        public List<Question> listQuestions() {
            return List.of();
        }

        @Override
        public List<Question> searchQuestions(QuestionCriteria criteria) {
            return List.of();
        }

        @Override
        public Optional<Question> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Question createQuestion(QuestionDraft draft, String username) {
            if (createFailure != null) {
                throw createFailure;
            }
            throw new AssertionError("La prueba debe configurar createFailure");
        }

        @Override
        public Question updateQuestion(String id, QuestionDraft draft, String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean changeState(String id, QuestionState newState, String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<QuestionStateChange> stateHistory(String id) {
            return List.of();
        }

        @Override
        public QuestionStatistics getStatistics() {
            return new QuestionStatistics(null);
        }
    }
}
