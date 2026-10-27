package co.edu.unicauca.questionbank.application;

/** Signals that a requested resource (user, topic, question, ...) does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param resource kind of resource, for example "Question"
     * @param id       identifier that was not found
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found: " + id);
    }
}
