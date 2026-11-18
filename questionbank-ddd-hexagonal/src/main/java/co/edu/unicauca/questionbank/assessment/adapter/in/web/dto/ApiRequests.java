package co.edu.unicauca.questionbank.assessment.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** HTTP request bodies. Validation annotations stay in the web adapter, outside the core. */
public final class ApiRequests {

    private ApiRequests() {
    }

    /**
     * JSON body to start an attempt.
     *
     * @param quizId    quiz to take
     * @param studentId student who takes it
     */
    public record StartAttemptRequest(@NotNull @Positive Long quizId, @NotNull @Positive Long studentId) {
    }

    /**
     * JSON body to answer a question.
     *
     * @param answer chosen answer: A..E for multiple choice, TRUE or FALSE for true/false
     */
    public record AnswerRequest(@NotBlank String answer) {
    }
}
