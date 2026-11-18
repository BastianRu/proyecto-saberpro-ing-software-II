package co.edu.unicauca.questionbank.assessment.domain.model;

import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;

import java.time.LocalDateTime;

/**
 * Entity owned by the {@link ExamAttempt} aggregate: the answer a student gave
 * to one question. It has its own identity, but it is only created or changed
 * through the aggregate root.
 */
public class SubmittedAnswer {

    private final AnswerId id;
    private final QuestionId questionId;
    private AnswerValue value;
    private LocalDateTime answeredAt;

    /**
     * Creates or rebuilds an answer.
     *
     * @param id         identity
     * @param questionId answered question
     * @param value      chosen answer
     * @param answeredAt moment of the last change
     */
    SubmittedAnswer(AnswerId id, QuestionId questionId, AnswerValue value, LocalDateTime answeredAt) {
        if (id == null || questionId == null || value == null || answeredAt == null) {
            throw new IllegalArgumentException("Answer data is required");
        }
        this.id = id;
        this.questionId = questionId;
        this.value = value;
        this.answeredAt = answeredAt;
    }

    /**
     * Rebuilds an answer from persisted state.
     *
     * @param id         identity
     * @param questionId answered question
     * @param value      chosen answer
     * @param answeredAt moment of the last change
     * @return the answer
     */
    public static SubmittedAnswer reconstitute(AnswerId id, QuestionId questionId, AnswerValue value,
                                               LocalDateTime answeredAt) {
        return new SubmittedAnswer(id, questionId, value, answeredAt);
    }

    /**
     * Replaces the chosen answer (only the aggregate root calls it).
     *
     * @param newValue new answer
     * @param now      moment of the change
     */
    void change(AnswerValue newValue, LocalDateTime now) {
        this.value = newValue;
        this.answeredAt = now;
    }

    /** @return identity */
    public AnswerId id() {
        return id;
    }

    /** @return answered question */
    public QuestionId questionId() {
        return questionId;
    }

    /** @return chosen answer */
    public AnswerValue value() {
        return value;
    }

    /** @return moment of the last change */
    public LocalDateTime answeredAt() {
        return answeredAt;
    }
}
