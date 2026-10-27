package co.edu.unicauca.questionbank.domain.model;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;

/** Small guard helpers shared by the domain classes. */
final class Require {

    private Require() {
    }

    static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("The " + field + " is required");
        }
        return value.trim();
    }

    static <T> T value(T value, String field) {
        if (value == null) {
            throw new BusinessRuleException("The " + field + " is required");
        }
        return value;
    }
}
