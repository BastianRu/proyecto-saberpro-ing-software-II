package co.edu.unicauca.questionbank.domain.exception;

import java.util.List;

/** Signals that the content of a question does not satisfy the academic rules. */
public class InvalidQuestionException extends BusinessRuleException {

    private final List<String> errors;

    /**
     * Creates the exception.
     *
     * @param errors every rule the question breaks
     */
    public InvalidQuestionException(List<String> errors) {
        super("Invalid question: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    /** @return every rule the question breaks */
    public List<String> getErrors() {
        return errors;
    }
}
