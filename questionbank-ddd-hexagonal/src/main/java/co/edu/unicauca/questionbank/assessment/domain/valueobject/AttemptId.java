package co.edu.unicauca.questionbank.assessment.domain.valueobject;

import java.util.UUID;

/**
 * Immutable identity of a mock exam attempt.
 *
 * @param value UUID that identifies the attempt
 */
public record AttemptId(UUID value) {

    /** Validates the identity. */
    public AttemptId {
        if (value == null) {
            throw new IllegalArgumentException("Attempt id is required");
        }
    }

    /** @return a new random identity */
    public static AttemptId newId() {
        return new AttemptId(UUID.randomUUID());
    }

    /**
     * Parses an identity from its external representation.
     *
     * @param value UUID text
     * @return the identity
     * @throws IllegalArgumentException if the text is not a UUID
     */
    public static AttemptId of(String value) {
        return new AttemptId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
