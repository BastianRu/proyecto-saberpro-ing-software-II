package co.edu.unicauca.questionbank.assessment.domain.valueobject;

import java.util.UUID;

/**
 * Immutable identity of an answer registered inside an attempt.
 *
 * @param value UUID that identifies the answer
 */
public record AnswerId(UUID value) {

    /** Validates the identity. */
    public AnswerId {
        if (value == null) {
            throw new IllegalArgumentException("Answer id is required");
        }
    }

    /** @return a new random identity */
    public static AnswerId newId() {
        return new AnswerId(UUID.randomUUID());
    }
}
