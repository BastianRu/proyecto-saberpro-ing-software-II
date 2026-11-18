package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/**
 * Identity of a student that lives in the question bank context. The assessment
 * context only keeps the identifier, never the student object itself.
 *
 * @param value numeric identifier assigned by the question bank
 */
public record StudentId(Long value) {

    /** Validates the identity. */
    public StudentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("A valid student id is required");
        }
    }

    /**
     * @param value numeric identifier
     * @return the identity
     */
    public static StudentId of(Long value) {
        return new StudentId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
