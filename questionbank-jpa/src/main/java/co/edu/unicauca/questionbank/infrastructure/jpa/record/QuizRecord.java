package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA record of a quiz (table {@code quizzes}). The questions of the quiz are
 * stored in the associative entity {@link QuizQuestionRecord}, because the
 * association needs its own attributes (position and points).
 */
@Entity
@Table(name = "quizzes")
public class QuizRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserRecord teacher;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuizQuestionRecord> questions = new ArrayList<>();

    /** Required by JPA. */
    protected QuizRecord() {
    }

    /**
     * @param title     title
     * @param teacher   teacher who organizes it
     * @param createdAt creation date
     */
    public QuizRecord(String title, UserRecord teacher, LocalDateTime createdAt) {
        this.title = title;
        this.teacher = teacher;
        this.createdAt = createdAt;
    }

    /**
     * Adds a question and sets the back reference.
     *
     * @param item associative record
     */
    public void addQuestion(QuizQuestionRecord item) {
        questions.add(item);
        item.setQuiz(this);
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return title */
    public String getTitle() {
        return title;
    }

    /** @return teacher */
    public UserRecord getTeacher() {
        return teacher;
    }

    /** @return creation date */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return questions of the quiz ordered by position */
    public List<QuizQuestionRecord> getQuestions() {
        return questions;
    }
}
