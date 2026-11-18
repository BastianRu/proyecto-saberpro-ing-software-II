package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** JPA record of a true/false question (table {@code true_false_questions}). */
@Entity
@Table(name = "true_false_questions")
@DiscriminatorValue("TRUE_FALSE")
public class TrueFalseQuestionRecord extends QuestionRecord {

    @Column(name = "correct_answer", nullable = false)
    private Boolean correctAnswer;

    /** Creates an empty record; the mapper fills it. */
    public TrueFalseQuestionRecord() {
    }

    /** @return expected truth value */
    public Boolean getCorrectAnswer() {
        return correctAnswer;
    }

    /** @param correctAnswer expected truth value */
    public void setCorrectAnswer(Boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
