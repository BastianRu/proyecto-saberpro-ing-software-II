package co.edu.unicauca.questionbank.assessment.adapter;

import co.edu.unicauca.questionbank.application.CatalogService;
import co.edu.unicauca.questionbank.application.QuestionService;
import co.edu.unicauca.questionbank.application.QuizService;
import co.edu.unicauca.questionbank.application.ReviewService;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the mock exam context: REST adapter -> input port ->
 * domain -> output adapters (JPA/H2 and the Workshop 10 question bank).
 * The quiz is built with the Workshop 10 use cases, so the test also proves that
 * both modules work together.
 */
@SpringBootTest(properties = "questionbank.sample-data.enabled=false")
@AutoConfigureMockMvc
class MockExamApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private CatalogService catalog;
    @Autowired
    private QuestionService questions;
    @Autowired
    private ReviewService reviews;
    @Autowired
    private QuizService quizzes;

    private long quizId;
    private long studentId;
    private long teacherId;
    private long mcId;
    private long tfId;

    @BeforeEach
    void buildQuizWithTheQuestionBank() {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        User author = catalog.registerUser("au" + tag, "Author", "au" + tag + "@t.co", Role.AUTHOR);
        User reviewer = catalog.registerUser("re" + tag, "Reviewer", "re" + tag + "@t.co", Role.REVIEWER);
        User teacher = catalog.registerUser("te" + tag, "Teacher", "te" + tag + "@t.co", Role.TEACHER);
        User student = catalog.registerUser("st" + tag, "Student", "st" + tag + "@t.co", Role.STUDENT);
        long topic = catalog.createTopic("Topic " + tag).getId();
        long competency = catalog.createCompetency("C" + tag, "Competency " + tag).getId();
        QuestionMetadata meta = new QuestionMetadata(CognitiveLevel.REMEMBER, 60, null, null);

        Question mc = questions.createMultipleChoice("Which pattern organizes a system in tiers?", Difficulty.LOW,
                author.getId(), List.of(AnswerOption.of("Microkernel", false), AnswerOption.of("Layers", true),
                        AnswerOption.of("Broker", false), AnswerOption.of("Pipes", false),
                        AnswerOption.of("P2P", false)), meta, topic, Set.of(competency));
        Question tf = questions.createTrueFalse("Domain entities must carry JPA annotations.", Difficulty.LOW,
                author.getId(), false, meta, topic, Set.of(competency));
        for (Question q : List.of(mc, tf)) {
            questions.submitForReview(q.getId());
            reviews.startReview(q.getId(), reviewer.getId());
            reviews.approve(q.getId(), reviewer.getId());
            questions.publish(q.getId());
        }
        Quiz quiz = quizzes.createQuiz("Mock " + tag, teacher.getId(), List.of(mc.getId(), tf.getId()));
        quizId = quiz.getId();
        studentId = student.getId();
        teacherId = teacher.getId();
        mcId = mc.getId();
        tfId = tf.getId();
    }

    private JsonNode body(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
        return json.readTree(actions.andReturn().getResponse().getContentAsString());
    }

    private String startAttempt(long student) throws Exception {
        JsonNode started = body(mvc.perform(post("/api/mock-exams/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quizId\":%d,\"studentId\":%d}".formatted(quizId, student)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.questions[0].choices[1].key").value("B"))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist()));
        return started.get("id").asText();
    }

    private void answer(String attempt, long question, String value, int expectedStatus) throws Exception {
        mvc.perform(put("/api/mock-exams/attempts/%s/answers/%d".formatted(attempt, question))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"answer\":\"" + value + "\"}"))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void studentTakesAQuizCreatedInTheQuestionBank() throws Exception {
        String attempt = startAttempt(studentId);

        answer(attempt, mcId, "B", 200);
        answer(attempt, tfId, "FALSE", 200);
        answer(attempt, tfId, "MAYBE", 422);

        mvc.perform(post("/api/mock-exams/attempts/" + attempt + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.score.earnedPoints").value(2.0))
                .andExpect(jsonPath("$.score.percentage").value(100.0))
                .andExpect(jsonPath("$.questions[1].correct").value(true));

        answer(attempt, mcId, "A", 422);
        mvc.perform(get("/api/mock-exams/students/" + studentId + "/attempts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].percentage").value(100.0));
    }

    @Test
    void businessRulesAreTranslatedToHttpErrors() throws Exception {
        // Only students: a teacher cannot take the quiz.
        mvc.perform(post("/api/mock-exams/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quizId\":%d,\"studentId\":%d}".formatted(quizId, teacherId)))
                .andExpect(status().isUnprocessableEntity());
        // Unknown quiz.
        mvc.perform(post("/api/mock-exams/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quizId\":99999,\"studentId\":%d}".formatted(studentId)))
                .andExpect(status().isNotFound());
        // Invalid body (web adapter validation).
        mvc.perform(post("/api/mock-exams/attempts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(2));
        // Malformed attempt id.
        mvc.perform(get("/api/mock-exams/attempts/not-a-uuid")).andExpect(status().isBadRequest());
        // One attempt in progress per quiz.
        startAttempt(studentId);
        mvc.perform(post("/api/mock-exams/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quizId\":%d,\"studentId\":%d}".formatted(quizId, studentId)))
                .andExpect(status().isUnprocessableEntity());
    }
}
