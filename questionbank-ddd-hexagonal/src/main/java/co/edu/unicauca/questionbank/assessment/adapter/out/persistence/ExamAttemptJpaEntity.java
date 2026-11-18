package co.edu.unicauca.questionbank.assessment.adapter.out.persistence;

import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptStatus;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptWindow;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.Score;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA persistence representation of the ExamAttempt aggregate. The student and
 * the quiz are stored as plain identifiers: the assessment context does not
 * create foreign keys to the tables of the question bank context.
 */
@Entity
@Table(name = "exam_attempts", indexes = @Index(name = "ix_attempt_student_quiz", columnList = "student_id, quiz_id"))
public class ExamAttemptJpaEntity {

    @Id
    UUID id;
    @Column(name = "student_id", nullable = false)
    Long studentId;
    @Column(name = "quiz_id", nullable = false)
    Long quizId;
    @Column(name = "started_at", nullable = false)
    LocalDateTime startedAt;
    @Column(nullable = false)
    LocalDateTime deadline;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AttemptStatus status;
    @Column(name = "submitted_at")
    LocalDateTime submittedAt;
    @Column(name = "earned_points")
    Double earnedPoints;
    @Column(name = "total_points")
    Double totalPoints;
    @Column(name = "correct_answers")
    Integer correctAnswers;
    @Column(name = "total_questions")
    Integer totalQuestions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "attempt_id", nullable = false)
    List<SubmittedAnswerJpaEntity> answers = new ArrayList<>();

    /** Required by JPA. */
    protected ExamAttemptJpaEntity() {
    }

    /**
     * Maps the complete aggregate, including its answers, to persistence.
     *
     * @param attempt domain aggregate
     * @return the JPA entity
     */
    static ExamAttemptJpaEntity fromDomain(ExamAttempt attempt) {
        ExamAttemptJpaEntity e = new ExamAttemptJpaEntity();
        e.id = attempt.id().value();
        e.studentId = attempt.studentId().value();
        e.quizId = attempt.quizId().value();
        e.startedAt = attempt.window().startedAt();
        e.deadline = attempt.window().deadline();
        e.status = attempt.status();
        e.submittedAt = attempt.submittedAt();
        attempt.score().ifPresent(score -> {
            e.earnedPoints = score.earnedPoints();
            e.totalPoints = score.totalPoints();
            e.correctAnswers = score.correctAnswers();
            e.totalQuestions = score.totalQuestions();
        });
        e.answers = attempt.answers().stream().map(SubmittedAnswerJpaEntity::fromDomain)
                .collect(Collectors.toCollection(ArrayList::new));
        return e;
    }

    /** @return the aggregate and its answers rebuilt from persisted state */
    ExamAttempt toDomain() {
        Score score = earnedPoints == null ? null
                : new Score(earnedPoints, totalPoints, correctAnswers, totalQuestions);
        return ExamAttempt.reconstitute(new AttemptId(id), StudentId.of(studentId), QuizId.of(quizId),
                new AttemptWindow(startedAt, deadline), status, submittedAt, score,
                answers.stream().map(SubmittedAnswerJpaEntity::toDomain).toList());
    }
}
