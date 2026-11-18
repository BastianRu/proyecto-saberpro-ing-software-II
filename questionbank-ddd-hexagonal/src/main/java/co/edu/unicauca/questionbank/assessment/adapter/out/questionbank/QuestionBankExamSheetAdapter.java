package co.edu.unicauca.questionbank.assessment.adapter.out.questionbank;

import co.edu.unicauca.questionbank.assessment.application.port.out.ExamSheetProviderPort;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamChoice;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamItem;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ItemKind;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.QuizItem;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;
import co.edu.unicauca.questionbank.domain.repository.QuestionRepository;
import co.edu.unicauca.questionbank.domain.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Secondary (driven) adapter and anti-corruption layer towards the question
 * bank module built in Workshop 10. It reads quizzes and questions through the
 * repository contracts of that module and translates them into the language of
 * the assessment context (exam sheet, items, choices and answer key). It is the
 * only class of the new context that knows the Workshop 10 domain model.
 *
 * <p>The time limit is the sum of the estimated times of the questions, but
 * never less than {@code questionbank.assessment.minimum-time-limit}.</p>
 */
@Component
public class QuestionBankExamSheetAdapter implements ExamSheetProviderPort {

    private static final String LETTERS = "ABCDEFGHIJ";

    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final Duration minimumTimeLimit;

    /**
     * @param quizzes          quiz repository contract of the question bank
     * @param questions        question repository contract of the question bank
     * @param minimumTimeLimit lower bound of the time limit of any exam
     */
    public QuestionBankExamSheetAdapter(QuizRepository quizzes, QuestionRepository questions,
                                        @Value("${questionbank.assessment.minimum-time-limit:PT20M}")
                                        Duration minimumTimeLimit) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.minimumTimeLimit = minimumTimeLimit;
    }

    @Override
    public Optional<ExamSheet> findByQuiz(QuizId quizId) {
        return quizzes.findById(quizId.value()).map(this::toSheet);
    }

    private ExamSheet toSheet(Quiz quiz) {
        List<ExamItem> items = new ArrayList<>();
        long seconds = 0;
        for (QuizItem quizItem : quiz.getItems()) {
            Optional<Question> found = questions.findById(quizItem.questionId());
            if (found.isEmpty()) {
                continue;
            }
            Question question = found.get();
            items.add(toItem(question, quizItem));
            seconds += question.getMetadata().getEstimatedTimeSeconds();
        }
        Duration timeLimit = items.isEmpty() ? Duration.ZERO : max(Duration.ofSeconds(seconds), minimumTimeLimit);
        return new ExamSheet(QuizId.of(quiz.getId()), quiz.getTitle(), items, timeLimit);
    }

    private static Duration max(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    private ExamItem toItem(Question question, QuizItem quizItem) {
        QuestionId id = QuestionId.of(question.getId());
        if (question instanceof MultipleChoiceQuestion mc) {
            List<ExamChoice> choices = new ArrayList<>();
            AnswerValue correct = null;
            List<AnswerOption> options = mc.getOptions();
            for (int i = 0; i < options.size(); i++) {
                AnswerValue key = AnswerValue.of(String.valueOf(LETTERS.charAt(i)));
                choices.add(new ExamChoice(key, options.get(i).getText()));
                if (options.get(i).isCorrect()) {
                    correct = key;
                }
            }
            return new ExamItem(id, quizItem.position(), quizItem.points(), mc.getStatement(),
                    ItemKind.MULTIPLE_CHOICE, choices, correct);
        }
        if (question instanceof TrueFalseQuestion tf) {
            List<ExamChoice> choices = List.of(new ExamChoice(AnswerValue.of("TRUE"), "Verdadero"),
                    new ExamChoice(AnswerValue.of("FALSE"), "Falso"));
            return new ExamItem(id, quizItem.position(), quizItem.points(), tf.getStatement(),
                    ItemKind.TRUE_FALSE, choices, AnswerValue.of(tf.getCorrectAnswer() ? "TRUE" : "FALSE"));
        }
        throw new IllegalStateException("Unsupported question type: " + question.getType());
    }
}
