package co.edu.unicauca.questionbank.assessment.domain.valueobject;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Time interval in which a student may answer an attempt.
 *
 * @param startedAt moment the attempt started
 * @param deadline  last moment an answer is accepted
 */
public record AttemptWindow(LocalDateTime startedAt, LocalDateTime deadline) {

    /** Ensures the interval is complete and has a positive duration. */
    public AttemptWindow {
        if (startedAt == null || deadline == null || !deadline.isAfter(startedAt)) {
            throw new IllegalArgumentException("The attempt deadline must be after its start");
        }
    }

    /**
     * Opens a window that lasts the time limit of the exam.
     *
     * @param startedAt start moment
     * @param timeLimit allowed duration (positive)
     * @return the window
     */
    public static AttemptWindow opening(LocalDateTime startedAt, Duration timeLimit) {
        if (timeLimit == null || timeLimit.isZero() || timeLimit.isNegative()) {
            throw new IllegalArgumentException("The time limit must be positive");
        }
        return new AttemptWindow(startedAt, startedAt.plus(timeLimit));
    }

    /**
     * @param now current moment
     * @return true if the deadline has passed
     */
    public boolean hasExpiredAt(LocalDateTime now) {
        return now.isAfter(deadline);
    }
}
