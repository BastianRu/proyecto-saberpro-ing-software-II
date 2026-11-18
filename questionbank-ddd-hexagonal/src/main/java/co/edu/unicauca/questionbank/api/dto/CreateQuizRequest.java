package co.edu.unicauca.questionbank.api.dto;

import java.util.List;

/**
 * Request body to create a quiz.
 *
 * @param title       title
 * @param teacherId   teacher identifier
 * @param questionIds questions to include, in order
 */
public record CreateQuizRequest(String title, Long teacherId, List<Long> questionIds) {
}
