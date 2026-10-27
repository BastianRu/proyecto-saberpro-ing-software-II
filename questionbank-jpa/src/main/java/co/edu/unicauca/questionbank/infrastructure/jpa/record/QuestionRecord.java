package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Base JPA record of the question hierarchy (table {@code questions}).
 *
 * <p>Inheritance uses the {@code JOINED} strategy: common columns live here and
 * each subtype has its own table whose primary key is also a foreign key to
 * {@code questions.id}. A discriminator column is added to make the H2 tables
 * easier to read.</p>
 *
 * <p>Relationships owned by this record:</p>
 * <ul>
 *     <li>many-to-one with {@link UserRecord} (author, column {@code author_id}, mandatory);</li>
 *     <li>many-to-one with {@link TopicRecord} (column {@code topic_id}, optional while drafting);</li>
 *     <li>many-to-many with {@link CompetencyRecord} through {@code question_competencies}.</li>
 * </ul>
 * The one-to-one with {@link QuestionMetadataRecord} is owned by the metadata side.
 */
@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "question_type")
public abstract class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String statement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserRecord author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private TopicRecord topic;

    @ManyToMany
    @JoinTable(name = "question_competencies",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "competency_id"))
    private Set<CompetencyRecord> competencies = new HashSet<>();

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private QuestionMetadataRecord metadata;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /** Required by JPA. */
    protected QuestionRecord() {
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return statement */
    public String getStatement() {
        return statement;
    }

    /** @param statement statement */
    public void setStatement(String statement) {
        this.statement = statement;
    }

    /** @return difficulty */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** @param difficulty difficulty */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /** @return lifecycle status */
    public QuestionStatus getStatus() {
        return status;
    }

    /** @param status lifecycle status */
    public void setStatus(QuestionStatus status) {
        this.status = status;
    }

    /** @return author */
    public UserRecord getAuthor() {
        return author;
    }

    /** @param author author (owning side of the many-to-one) */
    public void setAuthor(UserRecord author) {
        this.author = author;
    }

    /** @return topic, or null */
    public TopicRecord getTopic() {
        return topic;
    }

    /** @param topic topic (owning side of the many-to-one) */
    public void setTopic(TopicRecord topic) {
        this.topic = topic;
    }

    /** @return assessed competencies */
    public Set<CompetencyRecord> getCompetencies() {
        return competencies;
    }

    /** @param competencies new set of competencies (replaces the join table rows) */
    public void replaceCompetencies(Set<CompetencyRecord> competencies) {
        this.competencies.clear();
        this.competencies.addAll(competencies);
    }

    /** @return metadata, or null */
    public QuestionMetadataRecord getMetadata() {
        return metadata;
    }

    /** @param metadata metadata; also sets the back reference */
    public void setMetadata(QuestionMetadataRecord metadata) {
        this.metadata = metadata;
        if (metadata != null) {
            metadata.setQuestion(this);
        }
    }

    /** @return reason of the last rejection, or null */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /** @param rejectionReason reason of the last rejection */
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
