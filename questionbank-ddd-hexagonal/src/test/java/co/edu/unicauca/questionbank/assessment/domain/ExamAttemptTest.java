package co.edu.unicauca.questionbank.assessment.domain;

import co.edu.unicauca.questionbank.assessment.domain.exception.AssessmentRuleException;
import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptStatus;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.Score;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static co.edu.unicauca.questionbank.assessment.domain.ExamSheets.MC;
import static co.edu.unicauca.questionbank.assessment.domain.ExamSheets.TF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Invariants of the ExamAttempt aggregate, tested without Spring or a database. */
class ExamAttemptTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 17, 8, 0);

    private ExamSheet sheet;
    private ExamAttempt attempt;

    @BeforeEach
    void setUp() {
        sheet = ExamSheets.sample();
        attempt = ExamAttempt.start(AttemptId.newId(), StudentId.of(5L), sheet, START);
    }

    @Test
    void newAttemptIsInProgressAndDeadlineIsTheTimeLimit() {
        assertEquals(AttemptStatus.IN_PROGRESS, attempt.status());
        assertEquals(START.plusMinutes(10), attempt.window().deadline());
        assertTrue(attempt.score().isEmpty());
    }

    @Test
    void answeringAgainReplacesThePreviousAnswer() {
        attempt.answer(sheet, MC, AnswerValue.of("a"), START.plusMinutes(1));
        attempt.answer(sheet, MC, AnswerValue.of("B"), START.plusMinutes(2));
        assertEquals(1, attempt.answers().size());
        assertEquals("B", attempt.answerTo(MC).orElseThrow().value().value());
    }

    @Test
    void rejectsQuestionsOutsideTheExam() {
        assertThrows(AssessmentRuleException.class,
                () -> attempt.answer(sheet, QuestionId.of(99L), AnswerValue.of("A"), START));
    }

    @Test
    void rejectsAnswersThatAreNotChoices() {
        assertThrows(AssessmentRuleException.class,
                () -> attempt.answer(sheet, MC, AnswerValue.of("Z"), START));
        assertThrows(AssessmentRuleException.class,
                () -> attempt.answer(sheet, TF, AnswerValue.of("A"), START));
    }

    @Test
    void rejectsAnswersAfterTheDeadline() {
        assertThrows(AssessmentRuleException.class,
                () -> attempt.answer(sheet, MC, AnswerValue.of("B"), START.plusMinutes(11)));
    }

    @Test
    void submitScoresCorrectAnswersAndUnansweredCountAsWrong() {
        attempt.answer(sheet, TF, AnswerValue.of("false"), START.plusMinutes(1));
        Score score = attempt.submit(sheet, START.plusMinutes(3));
        assertEquals(2.0, score.earnedPoints());
        assertEquals(3.0, score.totalPoints());
        assertEquals(1, score.correctAnswers());
        assertEquals(66.7, score.percentage());
        assertEquals(AttemptStatus.SUBMITTED, attempt.status());
    }

    @Test
    void submittedAttemptIsFinal() {
        attempt.submit(sheet, START.plusMinutes(1));
        assertThrows(AssessmentRuleException.class, () -> attempt.submit(sheet, START.plusMinutes(2)));
        assertThrows(AssessmentRuleException.class,
                () -> attempt.answer(sheet, MC, AnswerValue.of("B"), START.plusMinutes(2)));
    }

    @Test
    void expiredAttemptIsClosedAtItsDeadline() {
        attempt.answer(sheet, MC, AnswerValue.of("B"), START.plusMinutes(1));
        assertFalse(attempt.closeIfExpired(sheet, START.plusMinutes(5)));
        assertTrue(attempt.closeIfExpired(sheet, START.plusMinutes(30)));
        assertEquals(START.plusMinutes(10), attempt.submittedAt());
        assertEquals(1.0, attempt.score().orElseThrow().earnedPoints());
    }
}
