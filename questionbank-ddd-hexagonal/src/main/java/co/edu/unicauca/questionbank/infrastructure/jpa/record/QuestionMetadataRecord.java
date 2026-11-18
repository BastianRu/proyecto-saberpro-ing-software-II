package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA record of the metadata of a question (table {@code question_metadata}).
 *
 * <p>It is the owning side of the one-to-one with {@link QuestionRecord}. With
 * {@code @MapsId} its primary key {@code question_id} is, at the same time, the
 * foreign key to {@code questions.id}; therefore a question can never have two
 * metadata rows and a metadata row can never exist without its question.</p>
 */
@Entity
@Table(name = "question_metadata")
public class QuestionMetadataRecord {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private QuestionRecord question;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_level", nullable = false, length = 20)
    private CognitiveLevel cognitiveLevel;

    @Column(name = "estimated_time_seconds", nullable = false)
    private int estimatedTimeSeconds;

    @Column(name = "editorial_notes", length = 1000)
    private String editorialNotes;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    /** Creates an empty record; the mapper fills it. */
    public QuestionMetadataRecord() {
    }

    /** @return identity (equal to the question identity) */
    public Long getId() {
        return id;
    }

    /** @return owning question */
    public QuestionRecord getQuestion() {
        return question;
    }

    /** @param question owning question */
    public void setQuestion(QuestionRecord question) {
        this.question = question;
    }

    /** @return cognitive level */
    public CognitiveLevel getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** @param cognitiveLevel cognitive level */
    public void setCognitiveLevel(CognitiveLevel cognitiveLevel) {
        this.cognitiveLevel = cognitiveLevel;
    }

    /** @return expected time in seconds */
    public int getEstimatedTimeSeconds() {
        return estimatedTimeSeconds;
    }

    /** @param estimatedTimeSeconds expected time in seconds */
    public void setEstimatedTimeSeconds(int estimatedTimeSeconds) {
        this.estimatedTimeSeconds = estimatedTimeSeconds;
    }

    /** @return editorial notes */
    public String getEditorialNotes() {
        return editorialNotes;
    }

    /** @param editorialNotes editorial notes */
    public void setEditorialNotes(String editorialNotes) {
        this.editorialNotes = editorialNotes;
    }

    /** @return date of the last review */
    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    /** @param lastReviewedAt date of the last review */
    public void setLastReviewedAt(LocalDateTime lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }
}
