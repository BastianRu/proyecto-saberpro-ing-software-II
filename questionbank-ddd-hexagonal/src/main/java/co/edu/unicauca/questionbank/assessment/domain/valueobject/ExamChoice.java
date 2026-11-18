package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/**
 * One choice shown to the student.
 *
 * @param key  value the student sends to select it (A..E, TRUE, FALSE)
 * @param text text displayed for the choice
 */
public record ExamChoice(AnswerValue key, String text) {

    /** Validates the choice. */
    public ExamChoice {
        if (key == null || text == null) {
            throw new IllegalArgumentException("Choice key and text are required");
        }
    }
}
