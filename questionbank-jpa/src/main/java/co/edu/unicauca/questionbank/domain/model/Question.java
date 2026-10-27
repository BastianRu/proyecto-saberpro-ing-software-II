package co.edu.unicauca.questionbank.domain.model;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.exception.InvalidQuestionException;
import co.edu.unicauca.questionbank.domain.exception.InvalidStatusTransitionException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class of every question in the bank. It holds the common data
 * (statement, difficulty, status, author, classification and metadata) and
 * enforces the lifecycle rules. Subclasses define the type-specific content.
 *
 * <p>Associations with other aggregates (author, topic, competencies) are kept
 * as identifiers, so the domain never depends on persistence objects.</p>
 */
public abstract class Question {

    private Long id;
    private final String statement;
    private final Difficulty difficulty;
    private QuestionStatus status;
    private final Long authorId;
    private Long topicId;
    private final Set<Long> competencyIds = new LinkedHashSet<>();
    private QuestionMetadata metadata;
    private String rejectionReason;

    /**
     * Rebuilds or creates a question. New questions should be created through the
     * factory methods of the subclasses, which start in {@link QuestionStatus#DRAFT}.
     *
     * @param id              identity, or null for a new question
     * @param statement       question statement
     * @param difficulty      difficulty level
     * @param status          current lifecycle status
     * @param authorId        identifier of the author
     * @param topicId         identifier of the topic, or null if not classified yet
     * @param competencyIds   identifiers of the assessed competencies
     * @param metadata        editorial metadata; defaults are used when null
     * @param rejectionReason reason of the last rejection, or null
     */
    protected Question(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                       Long authorId, Long topicId, Set<Long> competencyIds,
                       QuestionMetadata metadata, String rejectionReason) {
        this.id = id;
        this.statement = statement == null ? "" : statement.trim();
        this.difficulty = Require.value(difficulty, "difficulty");
        this.status = Require.value(status, "status");
        this.authorId = Require.value(authorId, "author");
        this.topicId = topicId;
        if (competencyIds != null) {
            this.competencyIds.addAll(competencyIds);
        }
        this.metadata = metadata == null ? QuestionMetadata.defaults() : metadata;
        this.rejectionReason = rejectionReason;
    }

    /** @return the concrete kind of question */
    public abstract QuestionType getType();

    /**
     * Type-specific content rules (options, correct answer, ...).
     *
     * @param errors list where every broken rule is appended
     */
    protected abstract void collectContentErrors(List<String> errors);

    /** @return every content rule this question breaks; empty if the content is valid */
    public List<String> contentErrors() {
        List<String> errors = new ArrayList<>();
        if (statement.isBlank()) {
            errors.add("The statement is required");
        }
        collectContentErrors(errors);
        return errors;
    }

    /**
     * Ensures the content is valid.
     *
     * @throws InvalidQuestionException if any content rule is broken
     */
    public void validateContent() {
        List<String> errors = contentErrors();
        if (!errors.isEmpty()) {
            throw new InvalidQuestionException(errors);
        }
    }

    /**
     * Associates the question with a topic and one or more competencies.
     * Only questions that are still being written (DRAFT) can be reclassified.
     *
     * @param topicId       topic identifier
     * @param competencyIds competency identifiers (at least one)
     */
    public void classify(Long topicId, Set<Long> competencyIds) {
        if (status != QuestionStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT questions can be classified, current status: " + status);
        }
        if (topicId == null) {
            throw new BusinessRuleException("The topic is required");
        }
        if (competencyIds == null || competencyIds.isEmpty()) {
            throw new BusinessRuleException("At least one competency is required");
        }
        this.topicId = topicId;
        this.competencyIds.clear();
        this.competencyIds.addAll(competencyIds);
    }

    /** @return true if the question has a topic and at least one competency */
    public boolean isClassified() {
        return topicId != null && !competencyIds.isEmpty();
    }

    // ---------------------------------------------------------------- lifecycle

    /**
     * DRAFT -> PENDING_REVIEW. Requires valid content and a complete classification.
     *
     * @throws InvalidQuestionException         if the minimum conditions are not met
     * @throws InvalidStatusTransitionException if the question is not a draft
     */
    public void submitForReview() {
        ensureTransition(QuestionStatus.PENDING_REVIEW);
        List<String> errors = contentErrors();
        if (!isClassified()) {
            errors.add("The question must have a topic and at least one competency");
        }
        if (!errors.isEmpty()) {
            throw new InvalidQuestionException(errors);
        }
        status = QuestionStatus.PENDING_REVIEW;
    }

    /** PENDING_REVIEW -> IN_REVIEW, when a reviewer takes the question. */
    public void startReview() {
        moveTo(QuestionStatus.IN_REVIEW);
    }

    /**
     * IN_REVIEW -> APPROVED.
     *
     * @param reviewedAt moment of the approval, stored in the metadata
     */
    public void approve(LocalDateTime reviewedAt) {
        moveTo(QuestionStatus.APPROVED);
        rejectionReason = null;
        metadata = metadata.reviewedAt(reviewedAt);
    }

    /**
     * IN_REVIEW -> REJECTED.
     *
     * @param reason     why the reviewer rejects the question (required)
     * @param reviewedAt moment of the rejection, stored in the metadata
     */
    public void reject(String reason, LocalDateTime reviewedAt) {
        String cleanReason = Require.text(reason, "rejection reason");
        moveTo(QuestionStatus.REJECTED);
        rejectionReason = cleanReason;
        metadata = metadata.reviewedAt(reviewedAt);
    }

    /** REJECTED -> DRAFT, so the author can correct the question and submit it again. */
    public void reopenForCorrection() {
        moveTo(QuestionStatus.DRAFT);
    }

    /** APPROVED -> PUBLISHED: the question becomes usable in quizzes. */
    public void publish() {
        moveTo(QuestionStatus.PUBLISHED);
    }

    /** PUBLISHED -> ARCHIVED: the question is no longer actively used. */
    public void archive() {
        moveTo(QuestionStatus.ARCHIVED);
    }

    /** @return true if the question can be included in a quiz (it was approved and published) */
    public boolean isAvailableForQuizzes() {
        return status == QuestionStatus.PUBLISHED;
    }

    private void moveTo(QuestionStatus target) {
        ensureTransition(target);
        status = target;
    }

    private void ensureTransition(QuestionStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(status, target);
        }
    }

    // ---------------------------------------------------------------- accessors

    /** @return identity, or null if not persisted yet */
    public Long getId() {
        return id;
    }

    /** @param id identity assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return statement */
    public String getStatement() {
        return statement;
    }

    /** @return difficulty level */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** @return current lifecycle status */
    public QuestionStatus getStatus() {
        return status;
    }

    /** @return author identifier */
    public Long getAuthorId() {
        return authorId;
    }

    /** @return topic identifier, or null if not classified */
    public Long getTopicId() {
        return topicId;
    }

    /** @return identifiers of the assessed competencies (read-only) */
    public Set<Long> getCompetencyIds() {
        return Set.copyOf(competencyIds);
    }

    /** @return editorial metadata */
    public QuestionMetadata getMetadata() {
        return metadata;
    }

    /** @return reason of the last rejection, or null */
    public String getRejectionReason() {
        return rejectionReason;
    }
}
