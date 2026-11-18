package co.edu.unicauca.questionbank.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a question:
 * DRAFT -> PENDING_REVIEW -> IN_REVIEW -> APPROVED -> PUBLISHED -> ARCHIVED.
 * A question in review may be REJECTED and then goes back to DRAFT for corrections.
 */
public enum QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED,
    ARCHIVED;

    /**
     * @param target requested status
     * @return true if the lifecycle allows moving from this status to {@code target}
     */
    public boolean canTransitionTo(QuestionStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<QuestionStatus> allowedTargets() {
        return switch (this) {
            case DRAFT -> EnumSet.of(PENDING_REVIEW);
            case PENDING_REVIEW -> EnumSet.of(IN_REVIEW);
            case IN_REVIEW -> EnumSet.of(APPROVED, REJECTED);
            case REJECTED -> EnumSet.of(DRAFT);
            case APPROVED -> EnumSet.of(PUBLISHED);
            case PUBLISHED -> EnumSet.of(ARCHIVED);
            case ARCHIVED -> EnumSet.noneOf(QuestionStatus.class);
        };
    }
}
