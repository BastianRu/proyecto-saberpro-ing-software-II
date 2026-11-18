package co.edu.unicauca.questionbank.assessment.application.service;

import co.edu.unicauca.questionbank.assessment.application.AssessmentNotFoundException;
import co.edu.unicauca.questionbank.assessment.application.dto.Commands.AnswerQuestionCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Commands.StartAttemptCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptResponse;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptSummaryResponse;
import co.edu.unicauca.questionbank.assessment.application.port.in.MockExamInputPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.AssessmentEventPublisherPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ClockPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ExamAttemptRepositoryPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ExamSheetProviderPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.StudentDirectoryPort;
import co.edu.unicauca.questionbank.assessment.domain.event.AssessmentEvent;
import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.service.AttemptPolicy;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentProfile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service of the assessment context. It implements the inbound
 * port: loads aggregates and exam sheets through the outbound ports, delegates
 * every business rule to the domain, asks for persistence and publishes the
 * domain events through an outbound port. It has no Spring
 * annotation; it is wired in {@code AssessmentConfiguration}.
 */
public class MockExamApplicationService implements MockExamInputPort {

    private final ExamAttemptRepositoryPort attempts;
    private final ExamSheetProviderPort sheets;
    private final StudentDirectoryPort students;
    private final ClockPort clock;
    private final AttemptPolicy policy;
    private final AssessmentEventPublisherPort events;

    /**
     * Creates the service.
     *
     * @param attempts attempt repository port
     * @param sheets   exam sheet provider port
     * @param students student directory port
     * @param clock    clock port
     * @param policy   domain policy to start attempts
     * @param events   event publisher port
     */
    public MockExamApplicationService(ExamAttemptRepositoryPort attempts, ExamSheetProviderPort sheets,
                                      StudentDirectoryPort students, ClockPort clock, AttemptPolicy policy,
                                      AssessmentEventPublisherPort events) {
        this.attempts = attempts;
        this.sheets = sheets;
        this.students = students;
        this.clock = clock;
        this.policy = policy;
        this.events = events;
    }

    @Override
    public AttemptResponse startAttempt(StartAttemptCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        QuizId quizId = QuizId.of(command.quizId());
        StudentProfile student = students.findById(studentId)
                .orElseThrow(() -> new AssessmentNotFoundException("Student", studentId));
        ExamSheet sheet = loadSheet(quizId);
        LocalDateTime now = clock.now();

        List<ExamAttempt> previous = attempts.findByStudentAndQuiz(studentId, quizId);
        for (ExamAttempt old : previous) {
            if (old.closeIfExpired(sheet, now)) {
                attempts.save(old);
                events.publish(AssessmentEvent.submitted(old));
            }
        }
        policy.ensureCanStart(student, sheet, previous);

        ExamAttempt attempt = attempts.save(ExamAttempt.start(AttemptId.newId(), studentId, sheet, now));
        events.publish(AssessmentEvent.started(attempt));
        return AttemptResponse.from(attempt, sheet);
    }

    @Override
    public AttemptResponse answerQuestion(AnswerQuestionCommand command) {
        ExamAttempt attempt = loadAttempt(command.attemptId());
        ExamSheet sheet = loadSheet(attempt.quizId());
        attempt.answer(sheet, QuestionId.of(command.questionId()), AnswerValue.of(command.answer()), clock.now());
        return AttemptResponse.from(attempts.save(attempt), sheet);
    }

    @Override
    public AttemptResponse submitAttempt(String attemptId) {
        ExamAttempt attempt = loadAttempt(attemptId);
        ExamSheet sheet = loadSheet(attempt.quizId());
        attempt.submit(sheet, clock.now());
        ExamAttempt saved = attempts.save(attempt);
        events.publish(AssessmentEvent.submitted(saved));
        return AttemptResponse.from(saved, sheet);
    }

    @Override
    public AttemptResponse getAttempt(String attemptId) {
        ExamAttempt attempt = loadAttempt(attemptId);
        ExamSheet sheet = loadSheet(attempt.quizId());
        if (attempt.closeIfExpired(sheet, clock.now())) {
            attempt = attempts.save(attempt);
            events.publish(AssessmentEvent.submitted(attempt));
        }
        return AttemptResponse.from(attempt, sheet);
    }

    @Override
    public List<AttemptSummaryResponse> listAttemptsOfStudent(Long studentId) {
        return attempts.findByStudent(StudentId.of(studentId)).stream()
                .map(AttemptSummaryResponse::from).toList();
    }

    private ExamAttempt loadAttempt(String attemptId) {
        return attempts.findById(AttemptId.of(attemptId))
                .orElseThrow(() -> new AssessmentNotFoundException("Attempt", attemptId));
    }

    private ExamSheet loadSheet(QuizId quizId) {
        return sheets.findByQuiz(quizId).orElseThrow(() -> new AssessmentNotFoundException("Quiz", quizId));
    }
}
