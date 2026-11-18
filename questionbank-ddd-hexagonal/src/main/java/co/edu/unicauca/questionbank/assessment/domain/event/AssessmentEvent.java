package co.edu.unicauca.questionbank.assessment.domain.event;

import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.Score;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when something relevant happens to an attempt. It is a
 * plain record, so any adapter (log, RabbitMQ, ...) can serialize it.
 *
 * @param eventType    ATTEMPT_STARTED or ATTEMPT_SUBMITTED
 * @param attemptId    attempt identity
 * @param studentId    student identity
 * @param quizId       quiz identity
 * @param earnedPoints points obtained, or null for a started attempt
 * @param totalPoints  maximum points, or null for a started attempt
 * @param percentage   score percentage, or null for a started attempt
 * @param occurredAt   moment the event happened
 */
public record AssessmentEvent(String eventType, UUID attemptId, Long studentId, Long quizId,
                              Double earnedPoints, Double totalPoints, Double percentage,
                              LocalDateTime occurredAt) {

    /** Type of the event emitted when an attempt starts. */
    public static final String ATTEMPT_STARTED = "ATTEMPT_STARTED";
    /** Type of the event emitted when an attempt is handed in or closed by time. */
    public static final String ATTEMPT_SUBMITTED = "ATTEMPT_SUBMITTED";

    /**
     * @param attempt attempt that has just started
     * @return the event
     */
    public static AssessmentEvent started(ExamAttempt attempt) {
        return new AssessmentEvent(ATTEMPT_STARTED, attempt.id().value(), attempt.studentId().value(),
                attempt.quizId().value(), null, null, null, attempt.window().startedAt());
    }

    /**
     * @param attempt attempt that has just been submitted
     * @return the event with the final score
     */
    public static AssessmentEvent submitted(ExamAttempt attempt) {
        Score score = attempt.score().orElseThrow(() -> new IllegalStateException("Attempt without score"));
        return new AssessmentEvent(ATTEMPT_SUBMITTED, attempt.id().value(), attempt.studentId().value(),
                attempt.quizId().value(), score.earnedPoints(), score.totalPoints(), score.percentage(),
                attempt.submittedAt());
    }
}
