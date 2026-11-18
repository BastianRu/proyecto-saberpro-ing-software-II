package co.edu.unicauca.questionbank.assessment.domain.valueobject;

import java.util.Locale;

/**
 * Answer chosen by a student, normalized to upper case. For a multiple choice
 * question it is the option letter ({@code A}..{@code E}); for a true/false
 * question it is {@code TRUE} or {@code FALSE}.
 *
 * @param value normalized answer key
 */
public record AnswerValue(String value) {

    /** Validates and normalizes the answer. */
    public AnswerValue {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("An answer is required");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * @param value raw answer
     * @return the normalized answer
     */
    public static AnswerValue of(String value) {
        return new AnswerValue(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
