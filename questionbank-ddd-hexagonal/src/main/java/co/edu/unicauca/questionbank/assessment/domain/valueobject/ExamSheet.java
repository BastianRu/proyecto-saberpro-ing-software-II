package co.edu.unicauca.questionbank.assessment.domain.valueobject;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Snapshot of a quiz as a mock exam: its questions, answer key and time limit.
 * It is produced by an output port from the question bank and is never stored
 * by the assessment context.
 *
 * @param quizId    quiz identity in the question bank
 * @param title     quiz title
 * @param items     questions in order
 * @param timeLimit time the student has to answer
 */
public record ExamSheet(QuizId quizId, String title, List<ExamItem> items, Duration timeLimit) {

    /** Validates the sheet and makes the items immutable. */
    public ExamSheet {
        if (quizId == null || title == null || timeLimit == null || timeLimit.isNegative()) {
            throw new IllegalArgumentException("Exam sheet data is required");
        }
        items = items == null ? List.of() : List.copyOf(items);
    }

    /**
     * @param questionId question identity
     * @return the item for that question, if it belongs to the exam
     */
    public Optional<ExamItem> item(QuestionId questionId) {
        return items.stream().filter(i -> i.questionId().equals(questionId)).findFirst();
    }

    /** @return true if the exam has no questions */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** @return sum of the points of every question */
    public double totalPoints() {
        return items.stream().mapToDouble(ExamItem::points).sum();
    }
}
