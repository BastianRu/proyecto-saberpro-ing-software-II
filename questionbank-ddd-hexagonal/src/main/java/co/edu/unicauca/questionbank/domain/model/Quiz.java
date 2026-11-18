package co.edu.unicauca.questionbank.domain.model;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A quiz (mock exam) organized by a teacher with published questions. */
public class Quiz {

    private Long id;
    private final String title;
    private final Long teacherId;
    private final LocalDateTime createdAt;
    private final List<QuizItem> items = new ArrayList<>();

    /**
     * Rebuilds or creates a quiz.
     *
     * @param id        identity, or null for a new quiz
     * @param title     title
     * @param teacherId identifier of the teacher who organizes it
     * @param createdAt creation date
     * @param items     questions already in the quiz
     */
    public Quiz(Long id, String title, Long teacherId, LocalDateTime createdAt, List<QuizItem> items) {
        this.id = id;
        this.title = Require.text(title, "quiz title");
        this.teacherId = Require.value(teacherId, "teacher");
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        if (items != null) {
            this.items.addAll(items);
        }
    }

    /**
     * Adds a question at the end of the quiz.
     *
     * @param question question to add
     * @param points   points for a correct answer (positive)
     * @throws BusinessRuleException if the question is not published, is repeated
     *                               or the points are not positive
     */
    public void addQuestion(Question question, double points) {
        if (question.getId() == null) {
            throw new BusinessRuleException("Only stored questions can be added to a quiz");
        }
        if (!question.isAvailableForQuizzes()) {
            throw new BusinessRuleException("Question " + question.getId()
                    + " cannot be used in a quiz because its status is " + question.getStatus()
                    + " (it must be approved and PUBLISHED)");
        }
        if (contains(question.getId())) {
            throw new BusinessRuleException("Question " + question.getId() + " is already in the quiz");
        }
        if (points <= 0) {
            throw new BusinessRuleException("Points must be positive");
        }
        items.add(new QuizItem(question.getId(), items.size() + 1, points));
    }

    /**
     * @param questionId question identifier
     * @return true if the question is already in the quiz
     */
    public boolean contains(Long questionId) {
        return items.stream().anyMatch(item -> item.questionId().equals(questionId));
    }

    /** @return sum of the points of every question */
    public double totalPoints() {
        return items.stream().mapToDouble(QuizItem::points).sum();
    }

    /** @return identity, or null if not persisted yet */
    public Long getId() {
        return id;
    }

    /** @param id identity assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return title */
    public String getTitle() {
        return title;
    }

    /** @return identifier of the teacher */
    public Long getTeacherId() {
        return teacherId;
    }

    /** @return creation date */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return questions of the quiz in order (read-only) */
    public List<QuizItem> getItems() {
        return List.copyOf(items);
    }
}
