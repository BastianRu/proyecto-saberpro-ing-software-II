package co.edu.unicauca.questionbank.assessment.adapter.out.persistence;

import co.edu.unicauca.questionbank.assessment.domain.model.SubmittedAnswer;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** JPA persistence representation of a SubmittedAnswer child entity (not a domain entity). */
@Entity
@Table(name = "exam_answers")
public class SubmittedAnswerJpaEntity {

    @Id
    UUID id;
    @Column(name = "question_id", nullable = false)
    Long questionId;
    @Column(name = "answer_value", nullable = false, length = 10)
    String value;
    @Column(name = "answered_at", nullable = false)
    LocalDateTime answeredAt;

    /** Required by JPA. */
    protected SubmittedAnswerJpaEntity() {
    }

    /**
     * Maps a domain answer to its persistence state.
     *
     * @param answer domain answer
     * @return the JPA entity
     */
    static SubmittedAnswerJpaEntity fromDomain(SubmittedAnswer answer) {
        SubmittedAnswerJpaEntity e = new SubmittedAnswerJpaEntity();
        e.id = answer.id().value();
        e.questionId = answer.questionId().value();
        e.value = answer.value().value();
        e.answeredAt = answer.answeredAt();
        return e;
    }

    /** @return the domain answer rebuilt from persisted state */
    SubmittedAnswer toDomain() {
        return SubmittedAnswer.reconstitute(new AnswerId(id), QuestionId.of(questionId), AnswerValue.of(value),
                answeredAt);
    }
}
