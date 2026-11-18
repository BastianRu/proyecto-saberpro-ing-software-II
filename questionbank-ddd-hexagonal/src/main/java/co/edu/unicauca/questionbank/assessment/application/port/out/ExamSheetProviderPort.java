package co.edu.unicauca.questionbank.assessment.application.port.out;

import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;

import java.util.Optional;

/**
 * Secondary (outbound) port to obtain a quiz as an exam sheet. The application
 * does not know that the quizzes live in the question bank module.
 */
public interface ExamSheetProviderPort {

    /**
     * @param quizId quiz identity
     * @return the exam sheet, if the quiz exists
     */
    Optional<ExamSheet> findByQuiz(QuizId quizId);
}
