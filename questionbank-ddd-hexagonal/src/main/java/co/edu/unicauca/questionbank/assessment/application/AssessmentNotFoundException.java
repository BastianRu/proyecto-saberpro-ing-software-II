package co.edu.unicauca.questionbank.assessment.application;

/** Signals that an attempt, quiz or student requested by a use case does not exist. */
public class AssessmentNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param resource kind of resource, for example "Attempt"
     * @param id       identifier that was not found
     */
    public AssessmentNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
