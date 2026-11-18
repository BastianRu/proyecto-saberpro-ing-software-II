package co.edu.unicauca.questionbank.assessment.domain.exception;

/**
 * Signals that an operation violates a rule of the assessment (mock exam)
 * context. It is a plain Java exception: the domain knows nothing about HTTP.
 */
public class AssessmentRuleException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message description of the violated rule
     */
    public AssessmentRuleException(String message) {
        super(message);
    }
}
