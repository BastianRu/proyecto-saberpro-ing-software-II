package co.edu.unicauca.questionbank.domain.model;

/** One answer option of a multiple choice question: a distractor or the correct answer. */
public class AnswerOption {

    private final Long id;
    private final String text;
    private final boolean correct;

    /**
     * Creates an option.
     *
     * @param id      identity, or null for a new option
     * @param text    text shown to the student
     * @param correct true if this is the correct answer
     */
    public AnswerOption(Long id, String text, boolean correct) {
        this.id = id;
        this.text = text == null ? "" : text.trim();
        this.correct = correct;
    }

    /**
     * Convenience factory for new options.
     *
     * @param text    option text
     * @param correct true if correct
     * @return the option without identity
     */
    public static AnswerOption of(String text, boolean correct) {
        return new AnswerOption(null, text, correct);
    }

    /** @return identity, or null if not persisted yet */
    public Long getId() {
        return id;
    }

    /** @return option text */
    public String getText() {
        return text;
    }

    /** @return true if this option is the correct answer */
    public boolean isCorrect() {
        return correct;
    }
}
