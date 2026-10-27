package co.edu.unicauca.questionbank.domain;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Quiz rules: only published questions, without repetitions. */
class QuizTest {

    private Question published(long id) {
        Question question = SampleQuestions.trueFalse(1L);
        question.setId(id);
        question.classify(1L, Set.of(1L));
        question.submitForReview();
        question.startReview();
        question.approve(LocalDateTime.now());
        question.publish();
        return question;
    }

    @Test
    void publishedQuestionsAreAddedInOrder() {
        Quiz quiz = new Quiz(null, "Mock exam", 4L, null, List.of());
        quiz.addQuestion(published(1), 1.0);
        quiz.addQuestion(published(2), 2.0);
        assertEquals(2, quiz.getItems().get(1).position());
        assertEquals(3.0, quiz.totalPoints());
    }

    @Test
    void unpublishedQuestionIsRejected() {
        Quiz quiz = new Quiz(null, "Mock exam", 4L, null, List.of());
        Question draft = SampleQuestions.trueFalse(1L);
        draft.setId(9L);
        assertThrows(BusinessRuleException.class, () -> quiz.addQuestion(draft, 1.0));
    }

    @Test
    void repeatedQuestionIsRejected() {
        Quiz quiz = new Quiz(null, "Mock exam", 4L, null, List.of());
        quiz.addQuestion(published(1), 1.0);
        assertThrows(BusinessRuleException.class, () -> quiz.addQuestion(published(1), 1.0));
    }
}
