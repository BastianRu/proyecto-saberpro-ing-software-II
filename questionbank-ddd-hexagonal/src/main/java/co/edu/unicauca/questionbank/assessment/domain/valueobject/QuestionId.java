package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/**
 * Identity of a question that lives in the question bank context. The assessment
 * context only keeps the identifier, never the question object itself.
 *
 * @param value numeric identifier assigned by the question bank
 */
public record QuestionId(Long value) {

    /** Validates the identity. */
    public QuestionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("A valid question id is required");
        }
    }

    /**
     * @param value numeric identifier
     * @return the identity
     */
    public static QuestionId of(Long value) {
        return new QuestionId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
