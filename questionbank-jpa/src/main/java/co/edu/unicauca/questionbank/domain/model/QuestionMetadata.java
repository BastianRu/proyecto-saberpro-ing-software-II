package co.edu.unicauca.questionbank.domain.model;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;

import java.time.LocalDateTime;

/**
 * Complementary editorial data of a question. Every question owns exactly one
 * metadata object (one-to-one relationship).
 */
public class QuestionMetadata {

    private final CognitiveLevel cognitiveLevel;
    private final int estimatedTimeSeconds;
    private final String editorialNotes;
    private final LocalDateTime lastReviewedAt;

    /**
     * Creates the metadata.
     *
     * @param cognitiveLevel       cognitive level the question targets
     * @param estimatedTimeSeconds expected time to answer, in seconds (must be positive)
     * @param editorialNotes       free editorial notes, may be null
     * @param lastReviewedAt       date of the last review, or null if never reviewed
     */
    public QuestionMetadata(CognitiveLevel cognitiveLevel, int estimatedTimeSeconds,
                            String editorialNotes, LocalDateTime lastReviewedAt) {
        this.cognitiveLevel = Require.value(cognitiveLevel, "cognitive level");
        if (estimatedTimeSeconds <= 0) {
            throw new BusinessRuleException("The estimated time must be positive");
        }
        this.estimatedTimeSeconds = estimatedTimeSeconds;
        this.editorialNotes = editorialNotes;
        this.lastReviewedAt = lastReviewedAt;
    }

    /** @return default metadata used when the author does not provide any */
    public static QuestionMetadata defaults() {
        return new QuestionMetadata(CognitiveLevel.UNDERSTAND, 120, null, null);
    }

    /**
     * @param reviewedAt moment of the review
     * @return a copy of this metadata with an updated review date
     */
    public QuestionMetadata reviewedAt(LocalDateTime reviewedAt) {
        return new QuestionMetadata(cognitiveLevel, estimatedTimeSeconds, editorialNotes, reviewedAt);
    }

    /** @return cognitive level */
    public CognitiveLevel getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** @return expected time to answer, in seconds */
    public int getEstimatedTimeSeconds() {
        return estimatedTimeSeconds;
    }

    /** @return editorial notes, or null */
    public String getEditorialNotes() {
        return editorialNotes;
    }

    /** @return date of the last review, or null */
    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }
}
