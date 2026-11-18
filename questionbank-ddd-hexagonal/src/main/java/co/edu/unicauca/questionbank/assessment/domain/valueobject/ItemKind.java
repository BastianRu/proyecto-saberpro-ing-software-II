package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/** Kind of question as seen by a student taking a mock exam. */
public enum ItemKind {
    /** One correct option among five (A..E). */
    MULTIPLE_CHOICE,
    /** The student decides whether the statement is TRUE or FALSE. */
    TRUE_FALSE
}
