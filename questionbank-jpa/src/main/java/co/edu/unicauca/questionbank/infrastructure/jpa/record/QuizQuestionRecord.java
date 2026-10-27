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
 * Associative entity between quizzes and questions (table {@code quiz_questions}).
 * Instead of a plain {@code @ManyToMany}, the association is split into two
 * many-to-one relationships so it can store the position and the points of
 * each question. A question cannot appear twice in the same quiz.
 */
@Entity
@Table(name = "quiz_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "question_id"}))
public class QuizQuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizRecord quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionRecord question;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private double points;

    /** Required by JPA. */
    protected QuizQuestionRecord() {
    }

    /**
     * @param question question included in the quiz
     * @param position order inside the quiz
     * @param points   points for a correct answer
     */
    public QuizQuestionRecord(QuestionRecord question, int position, double points) {
        this.question = question;
        this.position = position;
        this.points = points;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return owning quiz */
    public QuizRecord getQuiz() {
        return quiz;
    }

    /** @param quiz owning quiz */
    public void setQuiz(QuizRecord quiz) {
        this.quiz = quiz;
    }

    /** @return included question */
    public QuestionRecord getQuestion() {
        return question;
    }

    /** @return order inside the quiz */
    public int getPosition() {
        return position;
    }

    /** @return points for a correct answer */
    public double getPoints() {
        return points;
    }
}
