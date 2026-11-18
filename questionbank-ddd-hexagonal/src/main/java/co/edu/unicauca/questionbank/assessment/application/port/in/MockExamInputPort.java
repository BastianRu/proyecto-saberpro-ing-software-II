package co.edu.unicauca.questionbank.assessment.application.port.in;

import co.edu.unicauca.questionbank.assessment.application.dto.Commands.AnswerQuestionCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Commands.StartAttemptCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptResponse;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptSummaryResponse;

import java.util.List;

/**
 * Primary (inbound) port: the use cases a student has to take a mock exam.
 * It is independent of HTTP, Spring MVC and the persistence technology.
 */
public interface MockExamInputPort {

    /**
     * Starts an attempt at a quiz.
     *
     * @param command quiz and student
     * @return the new attempt with its questions (without answer key)
     */
    AttemptResponse startAttempt(StartAttemptCommand command);

    /**
     * Registers or replaces the answer to one question.
     *
     * @param command attempt, question and answer
     * @return the updated attempt
     */
    AttemptResponse answerQuestion(AnswerQuestionCommand command);

    /**
     * Hands in an attempt and scores it.
     *
     * @param attemptId attempt identity
     * @return the submitted attempt with its score and the answer key
     */
    AttemptResponse submitAttempt(String attemptId);

    /**
     * Retrieves an attempt (the student checks the result).
     *
     * @param attemptId attempt identity
     * @return the attempt
     */
    AttemptResponse getAttempt(String attemptId);

    /**
     * Lists the attempts of a student, newest first.
     *
     * @param studentId student identity
     * @return the attempts
     */
    List<AttemptSummaryResponse> listAttemptsOfStudent(Long studentId);
}
