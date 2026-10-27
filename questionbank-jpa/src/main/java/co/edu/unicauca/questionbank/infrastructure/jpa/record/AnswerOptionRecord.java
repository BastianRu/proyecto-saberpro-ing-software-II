package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * JPA record of an answer option (table {@code answer_options}). It is the
 * owning side of the one-to-many with {@link MultipleChoiceQuestionRecord}:
 * the non-null foreign key {@code question_id} prevents options that belong
 * to no question, and the unique constraint prevents two options in the same
 * position.
 */
@Entity
@Table(name = "answer_options",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "position"}))
public class AnswerOptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private MultipleChoiceQuestionRecord question;

    /** Required by JPA. */
    protected AnswerOptionRecord() {
    }

    /**
     * @param text     option text
     * @param correct  true if correct
     * @param position display position, starting at 1
     */
    public AnswerOptionRecord(String text, boolean correct, int position) {
        this.text = text;
        this.correct = correct;
        this.position = position;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return option text */
    public String getText() {
        return text;
    }

    /** @param text option text */
    public void setText(String text) {
        this.text = text;
    }

    /** @return true if correct */
    public boolean isCorrect() {
        return correct;
    }

    /** @param correct true if correct */
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    /** @return display position */
    public int getPosition() {
        return position;
    }

    /** @return owning question */
    public MultipleChoiceQuestionRecord getQuestion() {
        return question;
    }

    /** @param question owning question */
    public void setQuestion(MultipleChoiceQuestionRecord question) {
        this.question = question;
    }
}
