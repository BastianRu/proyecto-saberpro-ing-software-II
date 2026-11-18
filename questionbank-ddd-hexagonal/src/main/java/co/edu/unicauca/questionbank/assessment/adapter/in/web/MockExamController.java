package co.edu.unicauca.questionbank.assessment.adapter.in.web;

import co.edu.unicauca.questionbank.assessment.adapter.in.web.dto.ApiRequests.AnswerRequest;
import co.edu.unicauca.questionbank.assessment.adapter.in.web.dto.ApiRequests.StartAttemptRequest;
import co.edu.unicauca.questionbank.assessment.application.dto.Commands.AnswerQuestionCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Commands.StartAttemptCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptResponse;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptSummaryResponse;
import co.edu.unicauca.questionbank.assessment.application.port.in.MockExamInputPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Primary (driving) REST adapter: translates HTTP requests into calls to the
 * inbound port. It owns the transaction boundary of this entry path, as in the
 * GymClass demo.
 */
@RestController
@RequestMapping("/api/mock-exams")
public class MockExamController {

    private final MockExamInputPort inputPort;

    /** @param inputPort inbound port (not the concrete application service) */
    public MockExamController(MockExamInputPort inputPort) {
        this.inputPort = inputPort;
    }

    /**
     * Starts an attempt.
     *
     * @param request quiz and student
     * @return the new attempt
     */
    @Transactional
    @PostMapping("/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptResponse start(@Valid @RequestBody StartAttemptRequest request) {
        return inputPort.startAttempt(new StartAttemptCommand(request.quizId(), request.studentId()));
    }

    /**
     * Answers (or changes the answer to) one question.
     *
     * @param attemptId  attempt identity
     * @param questionId question identity
     * @param request    chosen answer
     * @return the updated attempt
     */
    @Transactional
    @PutMapping("/attempts/{attemptId}/answers/{questionId}")
    public AttemptResponse answer(@PathVariable String attemptId, @PathVariable Long questionId,
                                  @Valid @RequestBody AnswerRequest request) {
        return inputPort.answerQuestion(new AnswerQuestionCommand(attemptId, questionId, request.answer()));
    }

    /**
     * Hands in an attempt.
     *
     * @param attemptId attempt identity
     * @return the scored attempt
     */
    @Transactional
    @PostMapping("/attempts/{attemptId}/submit")
    public AttemptResponse submit(@PathVariable String attemptId) {
        return inputPort.submitAttempt(attemptId);
    }

    /**
     * Retrieves an attempt (closes it first if its time is over).
     *
     * @param attemptId attempt identity
     * @return the attempt
     */
    @Transactional
    @GetMapping("/attempts/{attemptId}")
    public AttemptResponse get(@PathVariable String attemptId) {
        return inputPort.getAttempt(attemptId);
    }

    /**
     * Lists the attempts of a student.
     *
     * @param studentId student identity
     * @return attempts, newest first
     */
    @Transactional(readOnly = true)
    @GetMapping("/students/{studentId}/attempts")
    public List<AttemptSummaryResponse> listByStudent(@PathVariable Long studentId) {
        return inputPort.listAttemptsOfStudent(studentId);
    }
}
