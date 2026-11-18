package co.edu.unicauca.questionbank.assessment.domain.model;

import co.edu.unicauca.questionbank.assessment.domain.exception.AssessmentRuleException;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptStatus;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptWindow;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamItem;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.Score;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate root of the assessment context: one attempt of a student at a mock
 * exam. It owns the {@link SubmittedAnswer} entities and is the only entry
 * point to answer, submit and score, so it protects these invariants:
 * <ul>
 *     <li>Answers are accepted only while the attempt is IN_PROGRESS and before the deadline.</li>
 *     <li>Only questions of the exam can be answered, and only with one of their choices.</li>
 *     <li>A question has at most one answer; answering again replaces it.</li>
 *     <li>A submitted attempt is final and has exactly one score.</li>
 * </ul>
 */
public class ExamAttempt {

    private final AttemptId id;
    private final StudentId studentId;
    private final QuizId quizId;
    private final AttemptWindow window;
    private AttemptStatus status;
    private LocalDateTime submittedAt;
    private Score score;
    private final List<SubmittedAnswer> answers = new ArrayList<>();

    private ExamAttempt(AttemptId id, StudentId studentId, QuizId quizId, AttemptWindow window) {
        if (id == null || studentId == null || quizId == null || window == null) {
            throw new IllegalArgumentException("Attempt data is required");
        }
        this.id = id;
        this.studentId = studentId;
        this.quizId = quizId;
        this.window = window;
        this.status = AttemptStatus.IN_PROGRESS;
    }

    /**
     * Starts a new attempt. The deadline is the start plus the time limit of the exam.
     *
     * @param id        identity of the new attempt
     * @param studentId student who takes the exam
     * @param sheet     exam to take
     * @param now       start moment
     * @return the attempt, IN_PROGRESS and without answers
     */
    public static ExamAttempt start(AttemptId id, StudentId studentId, ExamSheet sheet, LocalDateTime now) {
        return new ExamAttempt(id, studentId, sheet.quizId(), AttemptWindow.opening(now, sheet.timeLimit()));
    }

    /**
     * Rebuilds an attempt from persisted state.
     *
     * @param id          identity
     * @param studentId   student
     * @param quizId      quiz
     * @param window      time window
     * @param status      status
     * @param submittedAt submission moment, or null
     * @param score       score, or null while in progress
     * @param answers     answers registered so far
     * @return the attempt
     */
    public static ExamAttempt reconstitute(AttemptId id, StudentId studentId, QuizId quizId, AttemptWindow window,
                                           AttemptStatus status, LocalDateTime submittedAt, Score score,
                                           List<SubmittedAnswer> answers) {
        ExamAttempt attempt = new ExamAttempt(id, studentId, quizId, window);
        attempt.status = status;
        attempt.submittedAt = submittedAt;
        attempt.score = score;
        attempt.answers.addAll(answers);
        return attempt;
    }

    /**
     * Registers (or replaces) the answer to one question.
     *
     * @param sheet      exam of this attempt
     * @param questionId answered question
     * @param value      chosen answer
     * @param now        current moment
     * @return the registered answer
     * @throws AssessmentRuleException if any invariant would be broken
     */
    public SubmittedAnswer answer(ExamSheet sheet, QuestionId questionId, AnswerValue value, LocalDateTime now) {
        ensureSameExam(sheet);
        ensureInProgress();
        if (window.hasExpiredAt(now)) {
            throw new AssessmentRuleException("The time limit of the attempt is over (deadline "
                    + window.deadline() + ")");
        }
        ExamItem item = sheet.item(questionId).orElseThrow(() -> new AssessmentRuleException(
                "Question " + questionId + " does not belong to quiz " + quizId));
        if (!item.accepts(value)) {
            throw new AssessmentRuleException("Answer " + value + " is not a choice of question " + questionId);
        }
        Optional<SubmittedAnswer> existing = answerTo(questionId);
        if (existing.isPresent()) {
            existing.get().change(value, now);
            return existing.get();
        }
        SubmittedAnswer answer = new SubmittedAnswer(AnswerId.newId(), questionId, value, now);
        answers.add(answer);
        return answer;
    }

    /**
     * Hands in the attempt and computes its score. Unanswered questions count as wrong.
     *
     * @param sheet exam of this attempt (provides the answer key)
     * @param now   submission moment
     * @return the final score
     * @throws AssessmentRuleException if the attempt was already submitted
     */
    public Score submit(ExamSheet sheet, LocalDateTime now) {
        ensureSameExam(sheet);
        ensureInProgress();
        double earned = 0;
        int correct = 0;
        for (ExamItem item : sheet.items()) {
            Optional<SubmittedAnswer> answer = answerTo(item.questionId());
            if (answer.isPresent() && item.isCorrect(answer.get().value())) {
                earned += item.points();
                correct++;
            }
        }
        this.score = new Score(earned, sheet.totalPoints(), correct, sheet.items().size());
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = now;
        return score;
    }

    /**
     * Closes the attempt automatically if its time is over and it was not handed in.
     *
     * @param sheet exam of this attempt
     * @param now   current moment
     * @return true if the attempt was closed by this call
     */
    public boolean closeIfExpired(ExamSheet sheet, LocalDateTime now) {
        if (isInProgress() && window.hasExpiredAt(now)) {
            submit(sheet, window.deadline());
            return true;
        }
        return false;
    }

    /**
     * @param questionId question identity
     * @return the answer to that question, if any
     */
    public Optional<SubmittedAnswer> answerTo(QuestionId questionId) {
        return answers.stream().filter(a -> a.questionId().equals(questionId)).findFirst();
    }

    /** @return true while the student can still answer */
    public boolean isInProgress() {
        return status == AttemptStatus.IN_PROGRESS;
    }

    private void ensureInProgress() {
        if (!isInProgress()) {
            throw new AssessmentRuleException("Attempt " + id + " was already submitted");
        }
    }

    private void ensureSameExam(ExamSheet sheet) {
        if (sheet == null || !sheet.quizId().equals(quizId)) {
            throw new IllegalArgumentException("The exam sheet does not belong to this attempt");
        }
    }

    /** @return identity */
    public AttemptId id() {
        return id;
    }

    /** @return student who takes the exam */
    public StudentId studentId() {
        return studentId;
    }

    /** @return quiz being taken */
    public QuizId quizId() {
        return quizId;
    }

    /** @return time window */
    public AttemptWindow window() {
        return window;
    }

    /** @return current status */
    public AttemptStatus status() {
        return status;
    }

    /** @return submission moment, or null while in progress */
    public LocalDateTime submittedAt() {
        return submittedAt;
    }

    /** @return final score, or empty while in progress */
    public Optional<Score> score() {
        return Optional.ofNullable(score);
    }

    /** @return defensive copy of the answers */
    public List<SubmittedAnswer> answers() {
        return List.copyOf(answers);
    }
}
