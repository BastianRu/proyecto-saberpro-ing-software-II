package co.edu.unicauca.questionbank.domain;

import co.edu.unicauca.questionbank.domain.exception.InvalidQuestionException;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Content rules of the question types. */
class MultipleChoiceQuestionTest {

    @Test
    void validQuestionHasOneCorrectOption() {
        MultipleChoiceQuestion question = SampleQuestions.multipleChoice(1L);
        assertEquals("Layers", question.getCorrectOption().getText());
        assertTrue(question.contentErrors().isEmpty());
    }

    @Test
    void requiresExactlyFiveOptions() {
        List<AnswerOption> four = SampleQuestions.fiveOptions().subList(0, 4);
        assertThrows(InvalidQuestionException.class,
                () -> MultipleChoiceQuestion.draft("Statement?", Difficulty.LOW, 1L, four, null));
    }

    @Test
    void requiresExactlyOneCorrectOption() {
        List<AnswerOption> twoCorrect = List.of(AnswerOption.of("A", true), AnswerOption.of("B", true),
                AnswerOption.of("C", false), AnswerOption.of("D", false), AnswerOption.of("E", false));
        InvalidQuestionException ex = assertThrows(InvalidQuestionException.class,
                () -> MultipleChoiceQuestion.draft("Statement?", Difficulty.LOW, 1L, twoCorrect, null));
        assertTrue(ex.getErrors().get(0).contains("Exactly one option"));
    }

    @Test
    void rejectsDuplicatedOrBlankOptions() {
        List<AnswerOption> options = List.of(AnswerOption.of("A", true), AnswerOption.of("a", false),
                AnswerOption.of(" ", false), AnswerOption.of("D", false), AnswerOption.of("E", false));
        InvalidQuestionException ex = assertThrows(InvalidQuestionException.class,
                () -> MultipleChoiceQuestion.draft("Statement?", Difficulty.LOW, 1L, options, null));
        assertEquals(2, ex.getErrors().size());
    }

    @Test
    void statementIsRequiredForEveryType() {
        assertThrows(InvalidQuestionException.class,
                () -> TrueFalseQuestion.draft("  ", Difficulty.LOW, 1L, true, null));
    }
}
