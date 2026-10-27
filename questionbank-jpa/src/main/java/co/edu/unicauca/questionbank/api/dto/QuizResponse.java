package co.edu.unicauca.questionbank.api.dto;

import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.QuizItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JSON representation of a quiz.
 *
 * @param id          identifier
 * @param title       title
 * @param teacherId   teacher identifier
 * @param createdAt   creation date
 * @param questions   questions in order
 * @param totalPoints sum of the points
 */
public record QuizResponse(Long id, String title, Long teacherId, LocalDateTime createdAt,
                           List<QuizItem> questions, double totalPoints) {

    /**
     * @param quiz domain quiz
     * @return its JSON representation
     */
    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getTeacherId(), quiz.getCreatedAt(),
                quiz.getItems(), quiz.totalPoints());
    }
}
