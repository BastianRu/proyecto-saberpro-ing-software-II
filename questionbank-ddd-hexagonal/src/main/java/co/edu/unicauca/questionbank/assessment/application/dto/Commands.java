package co.edu.unicauca.questionbank.assessment.application.dto;

/** Framework-independent commands consumed by the mock exam use cases. */
public final class Commands {

    private Commands() {
    }

    /**
     * Input to start an attempt.
     *
     * @param quizId    quiz to take
     * @param studentId student who takes it
     */
    public record StartAttemptCommand(Long quizId, Long studentId) {
    }

    /**
     * Input to answer one question of an attempt.
     *
     * @param attemptId  attempt identity (UUID text)
     * @param questionId answered question
     * @param answer     chosen answer (A..E, TRUE or FALSE)
     */
    public record AnswerQuestionCommand(String attemptId, Long questionId, String answer) {
    }
}
