package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/**
 * Identity of a quiz that lives in the question bank context. The assessment
 * context only keeps the identifier, never the quiz object itself.
 *
 * @param value numeric identifier assigned by the question bank
 */
public record QuizId(Long value) {

    /** Validates the identity. */
    public QuizId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("A valid quiz id is required");
        }
    }

    /**
     * @param value numeric identifier
     * @return the identity
     */
    public static QuizId of(Long value) {
        return new QuizId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
