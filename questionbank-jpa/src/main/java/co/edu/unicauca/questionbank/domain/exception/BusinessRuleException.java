package co.edu.unicauca.questionbank.domain.exception;

/**
 * Signals that an operation violates a business rule of the question bank.
 * It belongs to the domain, so it carries no technology-specific information.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message human-readable description of the violated rule
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
