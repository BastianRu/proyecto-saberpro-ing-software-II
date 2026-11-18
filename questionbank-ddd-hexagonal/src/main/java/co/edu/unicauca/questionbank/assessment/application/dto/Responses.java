package co.edu.unicauca.questionbank.assessment.application.dto;

import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.model.SubmittedAnswer;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamItem;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.Score;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Response models returned through the inbound port. They never expose domain objects. */
public final class Responses {

    private Responses() {
    }

    /**
     * Full view of an attempt. While it is IN_PROGRESS the answer key is hidden
     * ({@code correctAnswer} and {@code correct} are null and there is no score).
     *
     * @param id                attempt identity
     * @param quizId            quiz
     * @param quizTitle         quiz title
     * @param studentId         student
     * @param status            IN_PROGRESS or SUBMITTED
     * @param startedAt         start moment
     * @param deadline          last moment to answer
     * @param submittedAt       submission moment, or null
     * @param answeredQuestions number of answered questions
     * @param totalQuestions    number of questions
     * @param score             score, or null while in progress
     * @param questions         questions with the student's answers
     */
    public record AttemptResponse(UUID id, Long quizId, String quizTitle, Long studentId, String status,
                                  LocalDateTime startedAt, LocalDateTime deadline, LocalDateTime submittedAt,
                                  int answeredQuestions, int totalQuestions, ScoreResponse score,
                                  List<ExamQuestionResponse> questions) {

        /**
         * Maps an attempt and its exam to the public representation.
         *
         * @param attempt attempt
         * @param sheet   exam of the attempt
         * @return the response
         */
        public static AttemptResponse from(ExamAttempt attempt, ExamSheet sheet) {
            boolean reveal = !attempt.isInProgress();
            List<ExamQuestionResponse> questions = sheet.items().stream()
                    .map(item -> ExamQuestionResponse.from(item, attempt, reveal)).toList();
            return new AttemptResponse(attempt.id().value(), attempt.quizId().value(), sheet.title(),
                    attempt.studentId().value(), attempt.status().name(), attempt.window().startedAt(),
                    attempt.window().deadline(), attempt.submittedAt(), attempt.answers().size(),
                    sheet.items().size(), attempt.score().map(ScoreResponse::from).orElse(null), questions);
        }
    }

    /**
     * A question of the attempt.
     *
     * @param questionId     question identity
     * @param position       order in the exam
     * @param points         points for a correct answer
     * @param statement      statement
     * @param kind           MULTIPLE_CHOICE or TRUE_FALSE
     * @param choices        choices
     * @param selectedAnswer answer of the student, or null
     * @param correctAnswer  answer key, only after submission
     * @param correct        whether the student was right, only after submission
     */
    public record ExamQuestionResponse(Long questionId, int position, double points, String statement,
                                       String kind, List<ChoiceResponse> choices, String selectedAnswer,
                                       String correctAnswer, Boolean correct) {

        static ExamQuestionResponse from(ExamItem item, ExamAttempt attempt, boolean reveal) {
            String selected = attempt.answerTo(item.questionId())
                    .map(SubmittedAnswer::value).map(Object::toString).orElse(null);
            List<ChoiceResponse> choices = item.choices().stream()
                    .map(c -> new ChoiceResponse(c.key().value(), c.text())).toList();
            return new ExamQuestionResponse(item.questionId().value(), item.position(), item.points(),
                    item.statement(), item.kind().name(), choices, selected,
                    reveal ? item.correctAnswer().value() : null,
                    reveal ? item.correctAnswer().value().equals(selected) : null);
        }
    }

    /**
     * A choice of a question.
     *
     * @param key  value to send when answering
     * @param text displayed text
     */
    public record ChoiceResponse(String key, String text) {
    }

    /**
     * Score of a submitted attempt.
     *
     * @param earnedPoints   points obtained
     * @param totalPoints    maximum points
     * @param correctAnswers correct answers
     * @param totalQuestions questions in the exam
     * @param percentage     earned points as a percentage
     */
    public record ScoreResponse(double earnedPoints, double totalPoints, int correctAnswers, int totalQuestions,
                                double percentage) {

        static ScoreResponse from(Score score) {
            return new ScoreResponse(score.earnedPoints(), score.totalPoints(), score.correctAnswers(),
                    score.totalQuestions(), score.percentage());
        }
    }

    /**
     * Short view used to list the attempts of a student.
     *
     * @param id          attempt identity
     * @param quizId      quiz
     * @param status      status
     * @param startedAt   start moment
     * @param submittedAt submission moment, or null
     * @param percentage  score percentage, or null while in progress
     */
    public record AttemptSummaryResponse(UUID id, Long quizId, String status, LocalDateTime startedAt,
                                         LocalDateTime submittedAt, Double percentage) {

        /**
         * @param attempt attempt
         * @return the summary
         */
        public static AttemptSummaryResponse from(ExamAttempt attempt) {
            return new AttemptSummaryResponse(attempt.id().value(), attempt.quizId().value(),
                    attempt.status().name(), attempt.window().startedAt(), attempt.submittedAt(),
                    attempt.score().map(Score::percentage).orElse(null));
        }
    }
}
