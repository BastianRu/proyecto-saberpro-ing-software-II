package com.unicauca.lisw2t2g08.banco.api;

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

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.banco.domain.Role;
import com.unicauca.lisw2t2g08.banco.security.ForbiddenException;
import com.unicauca.lisw2t2g08.banco.security.TokenVerifier;
import com.unicauca.lisw2t2g08.banco.security.UnauthorizedException;
import com.unicauca.lisw2t2g08.banco.service.IQuestionService;

import static org.junit.jupiter.api.Assertions.*;

class QuestionControllerTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private Clock fixedClock;
    private TokenVerifier tokenVerifier;
    private FakeQuestionService fakeService;
    private QuestionController controller;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneId.of("UTC"));
        tokenVerifier = new TokenVerifier(SECRET, fixedClock);
        fakeService = new FakeQuestionService();
        controller = new QuestionController(fakeService, tokenVerifier);
    }

    @Test
    void list_withoutFilters_returnsAllQuestions() {
        fakeService.storedQuestions.add(sampleQuestion("P-001", "Pregunta 1", QuestionState.BORRADOR));
        fakeService.storedQuestions.add(sampleQuestion("P-002", "Pregunta 2", QuestionState.APROBADA));

        List<QuestionResponse> response = controller.list(null, null, null, null, null, null);

        assertEquals(2, response.size());
        assertEquals("P-001", response.get(0).id());
        assertEquals("P-002", response.get(1).id());
    }

    @Test
    void findById_existingId_returnsQuestion() {
        fakeService.storedQuestions.add(sampleQuestion("P-001", "Pregunta 1", QuestionState.BORRADOR));

        QuestionResponse response = controller.findById("P-001");

        assertNotNull(response);
        assertEquals("P-001", response.id());
    }

    @Test
    void findById_nonExistingId_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> controller.findById("P-999"));
    }

    @Test
    void create_withoutToken_throwsUnauthorized() {
        QuestionRequest request = sampleRequest();

        assertThrows(UnauthorizedException.class, () -> controller.create(null, request));
        assertThrows(UnauthorizedException.class, () -> controller.create("", request));
    }

    @Test
    void create_withInsufficientRole_throwsForbidden() {
        String token = "Bearer " + createToken("estudiante1", Role.ESTUDIANTE);
        QuestionRequest request = sampleRequest();

        assertThrows(ForbiddenException.class, () -> controller.create(token, request));
    }

    @Test
    void create_withAutorRole_createsQuestion() {
        String token = "Bearer " + createToken("autor1", Role.AUTOR_PREGUNTAS);
        QuestionRequest request = sampleRequest();

        QuestionResponse response = controller.create(token, request);

        assertNotNull(response);
        assertEquals("P-001", response.id());
        assertEquals(QuestionState.BORRADOR, response.state());
    }

    @Test
    void changeState_withRevisorRole_updatesState() {
        fakeService.storedQuestions.add(sampleQuestion("P-001", "Pregunta 1", QuestionState.PENDIENTE_REVISION));
        String token = "Bearer " + createToken("revisor1", Role.REVISOR);

        ChangeStateRequest request = new ChangeStateRequest(QuestionState.EN_REVISION);
        QuestionResponse response = controller.changeState("P-001", token, request);

        assertEquals(QuestionState.EN_REVISION, response.state());
    }

    @Test
    void statistics_returnsStatisticsResponse() {
        fakeService.storedQuestions.add(sampleQuestion("P-001", "P1", QuestionState.BORRADOR));
        fakeService.storedQuestions.add(sampleQuestion("P-002", "P2", QuestionState.BORRADOR));

        QuestionStatisticsResponse stats = controller.statistics();

        assertEquals(2, stats.total());
        assertEquals(2, stats.counts().get(QuestionState.BORRADOR));
        assertEquals(100.0, stats.percentages().get(QuestionState.BORRADOR));
    }

    private QuestionRequest sampleRequest() {
        return new QuestionRequest(
                "Pregunta nueva",
                "Contexto de mas de 20 caracteres para validar.",
                "Cual es la correcta?",
                List.of("A", "B", "C", "D", "E"),
                0,
                "Justificacion",
                "Bibliografia",
                "Ingenieria",
                "Tema",
                "Subtema",
                DifficultyLevel.MEDIA);
    }

    private Question sampleQuestion(String id, String name, QuestionState state) {
        return new Question(
                id,
                name,
                "Contexto de ejemplo",
                "Enunciado?",
                new QuestionDistractors(List.of("A", "B", "C", "D", "E"), 0),
                "Justificacion",
                "Bibliografia",
                "Competencia",
                "Tema",
                "Subtema",
                DifficultyLevel.MEDIA,
                state);
    }

    private String createToken(String subject, Role role) {
        LocalDateTime future = LocalDateTime.now(fixedClock).plusMinutes(30);
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
        final List<Question> storedQuestions = new java.util.ArrayList<>();

        @Override
        public List<Question> listQuestions() {
            return storedQuestions;
        }

        @Override
        public List<Question> searchQuestions(QuestionCriteria criteria) {
            return storedQuestions.stream().filter(criteria::matches).toList();
        }

        @Override
        public Optional<Question> findById(String id) {
            return storedQuestions.stream().filter(q -> q.getId().equals(id)).findFirst();
        }

        @Override
        public Question createQuestion(QuestionDraft draft, String username) {
            Question q = new Question(
                    "P-001",
                    draft.getName(),
                    draft.getContext(),
                    draft.getDirectQuestion(),
                    new QuestionDistractors(draft.getOptions(), draft.getCorrectIndex()),
                    draft.getJustification(),
                    draft.getBibliography(),
                    draft.getCompetence(),
                    draft.getTopic(),
                    draft.getSubtopic(),
                    draft.getDifficulty(),
                    QuestionState.BORRADOR);
            storedQuestions.add(q);
            return q;
        }

        @Override
        public Question updateQuestion(String id, QuestionDraft draft, String username) {
            Question q = findById(id).orElseThrow();
            q.modifyContent(
                    draft.getName(),
                    draft.getContext(),
                    draft.getDirectQuestion(),
                    new QuestionDistractors(draft.getOptions(), draft.getCorrectIndex()),
                    draft.getJustification(),
                    draft.getBibliography(),
                    draft.getCompetence(),
                    draft.getTopic(),
                    draft.getSubtopic(),
                    draft.getDifficulty());
            return q;
        }

        @Override
        public boolean changeState(String id, QuestionState newState, String username) {
            Question q = findById(id).orElseThrow();
            q.changeState(newState);
            return true;
        }

        @Override
        public List<QuestionStateChange> stateHistory(String id) {
            return List.of();
        }

        @Override
        public QuestionStatistics getStatistics() {
            java.util.Map<QuestionState, Integer> counts = new java.util.EnumMap<>(QuestionState.class);
            for (QuestionState state : QuestionState.values()) {
                counts.put(state, 0);
            }
            for (Question q : storedQuestions) {
                counts.merge(q.getState(), 1, Integer::sum);
            }
            return new QuestionStatistics(counts);
        }
    }
}
