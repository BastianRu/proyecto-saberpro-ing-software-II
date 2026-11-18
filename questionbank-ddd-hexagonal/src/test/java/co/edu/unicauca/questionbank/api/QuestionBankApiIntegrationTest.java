package co.edu.unicauca.questionbank.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end test of the main use cases through the REST API and H2. */
@SpringBootTest(properties = "questionbank.sample-data.enabled=false")
@AutoConfigureMockMvc
class QuestionBankApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private JsonNode call(String url, String body, ResultMatcher expected) throws Exception {
        String response = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(expected).andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private long id(String url, String body) throws Exception {
        return call(url, body, status().isCreated()).get("id").asLong();
    }

    @Test
    void questionGoesFromCreationToQuiz() throws Exception {
        long author = id("/api/users", """
                {"username":"api.author","fullName":"Api Author","email":"api.author@test.co","role":"AUTHOR"}""");
        long reviewer = id("/api/users", """
                {"username":"api.reviewer","fullName":"Api Reviewer","email":"api.reviewer@test.co","role":"REVIEWER"}""");
        long teacher = id("/api/users", """
                {"username":"api.teacher","fullName":"Api Teacher","email":"api.teacher@test.co","role":"TEACHER"}""");
        long topic = id("/api/topics", "{\"name\":\"Api topic\"}");
        long competency = id("/api/competencies", "{\"code\":\"API\",\"name\":\"Api competency\"}");

        long question = id("/api/questions", """
                {"type":"MULTIPLE_CHOICE","statement":"Which pattern splits a system into tiers?",
                 "difficulty":"LOW","authorId":%d,
                 "options":[{"text":"Layers","correct":true},{"text":"Broker","correct":false},
                            {"text":"Microkernel","correct":false},{"text":"Pipes","correct":false},
                            {"text":"P2P","correct":false}]}""".formatted(author));

        // Not classified yet: the minimum conditions to submit are not met.
        call("/api/questions/" + question + "/submit", "", status().isBadRequest());
        // A draft cannot be approved.
        call("/api/questions/" + question + "/approve", "{\"reviewerId\":" + reviewer + "}", status().isConflict());

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/questions/" + question + "/classification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topicId\":%d,\"competencyIds\":[%d]}".formatted(topic, competency)))
                .andExpect(status().isOk());

        // Draft questions cannot be used in quizzes.
        call("/api/quizzes", "{\"title\":\"Too early\",\"teacherId\":%d,\"questionIds\":[%d]}"
                .formatted(teacher, question), status().isUnprocessableEntity());

        call("/api/questions/" + question + "/submit", "", status().isOk());
        call("/api/questions/" + question + "/start-review", "{\"reviewerId\":" + reviewer + "}", status().isOk());
        call("/api/questions/" + question + "/approve", "{\"reviewerId\":" + reviewer + "}", status().isOk());
        call("/api/questions/" + question + "/publish", "", status().isOk());

        call("/api/quizzes", "{\"title\":\"Mock exam\",\"teacherId\":%d,\"questionIds\":[%d]}"
                .formatted(teacher, question), status().isCreated());

        mvc.perform(get("/api/questions").param("competencyId", String.valueOf(competency)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(question))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].options.length()").value(5));
    }

    @Test
    void invalidQuestionReturnsEveryError() throws Exception {
        long author = id("/api/users", """
                {"username":"api.author2","fullName":"Api Author","email":"api.author2@test.co","role":"AUTHOR"}""");
        JsonNode error = call("/api/questions", """
                {"type":"MULTIPLE_CHOICE","statement":"Incomplete?","difficulty":"LOW","authorId":%d,
                 "options":[{"text":"A","correct":true},{"text":"B","correct":true}]}""".formatted(author),
                status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertEquals(2, error.get("details").size());

        mvc.perform(get("/api/questions/9999")).andExpect(status().isNotFound());
    }
}
