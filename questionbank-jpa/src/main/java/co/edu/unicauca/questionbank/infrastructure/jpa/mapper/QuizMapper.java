package co.edu.unicauca.questionbank.infrastructure.jpa.mapper;

import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.QuizItem;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuizRecord;

import java.util.List;

/** Converts quizzes from JPA records to domain objects. */
public final class QuizMapper {

    private QuizMapper() {
    }

    /**
     * @param record JPA record (read inside a transaction)
     * @return domain quiz
     */
    public static Quiz toDomain(QuizRecord record) {
        List<QuizItem> items = record.getQuestions().stream()
                .map(q -> new QuizItem(q.getQuestion().getId(), q.getPosition(), q.getPoints()))
                .toList();
        return new Quiz(record.getId(), record.getTitle(), record.getTeacher().getId(),
                record.getCreatedAt(), items);
    }
}
