package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/** Lifecycle of a mock exam attempt. */
public enum AttemptStatus {
    /** The student is answering; answers can still be registered or changed. */
    IN_PROGRESS,
    /** The attempt was handed in (or closed by time); it has a final score. */
    SUBMITTED
}
