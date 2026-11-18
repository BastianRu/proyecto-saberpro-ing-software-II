package co.edu.unicauca.questionbank.assessment.application;

import co.edu.unicauca.questionbank.assessment.application.dto.Commands.AnswerQuestionCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Commands.StartAttemptCommand;
import co.edu.unicauca.questionbank.assessment.application.dto.Responses.AttemptResponse;
import co.edu.unicauca.questionbank.assessment.application.port.in.MockExamInputPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ClockPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ExamAttemptRepositoryPort;
import co.edu.unicauca.questionbank.assessment.application.service.MockExamApplicationService;
import co.edu.unicauca.questionbank.assessment.domain.ExamSheets;
import co.edu.unicauca.questionbank.assessment.domain.event.AssessmentEvent;
import co.edu.unicauca.questionbank.assessment.domain.exception.AssessmentRuleException;
import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.service.AttemptPolicy;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the use cases with in-memory fake adapters (exercise 4 of the GymClass
 * README): the application service does not know which adapter it is using.
 */
class MockExamApplicationServiceTest {

    /** In-memory implementation of the attempt repository port. */
    static class InMemoryAttempts implements ExamAttemptRepositoryPort {
        private final Map<AttemptId, ExamAttempt> data = new LinkedHashMap<>();

        @Override
        public ExamAttempt save(ExamAttempt attempt) {
            data.put(attempt.id(), attempt);
            return attempt;
        }

        @Override
        public Optional<ExamAttempt> findById(AttemptId id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public List<ExamAttempt> findByStudentAndQuiz(StudentId studentId, QuizId quizId) {
            return data.values().stream()
                    .filter(a -> a.studentId().equals(studentId) && a.quizId().equals(quizId)).toList();
        }

        @Override
        public List<ExamAttempt> findByStudent(StudentId studentId) {
            return data.values().stream().filter(a -> a.studentId().equals(studentId))
                    .sorted(Comparator.comparing((ExamAttempt a) -> a.window().startedAt()).reversed()).toList();
        }
    }

    /** Clock that the test moves forward manually. */
    static class FixedClock implements ClockPort {
        LocalDateTime now = LocalDateTime.of(2026, 11, 17, 8, 0);

        @Override
        public LocalDateTime now() {
            return now;
        }
    }

    private FixedClock clock;
    private MockExamInputPort service;
    private final List<AssessmentEvent> published = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clock = new FixedClock();
        Map<Long, StudentProfile> people = Map.of(
                5L, new StudentProfile(StudentId.of(5L), "Juan Diaz", true),
                4L, new StudentProfile(StudentId.of(4L), "Maria Ruiz", false));
        service = new MockExamApplicationService(new InMemoryAttempts(),
                quizId -> quizId.equals(ExamSheets.QUIZ) ? Optional.of(ExamSheets.sample()) : Optional.empty(),
                id -> Optional.ofNullable(people.get(id.value())),
                clock, new AttemptPolicy(), published::add);
    }

    @Test
    void studentTakesAMockExamAndGetsTheScore() {
        AttemptResponse started = service.startAttempt(new StartAttemptCommand(1L, 5L));
        assertEquals("IN_PROGRESS", started.status());
        assertNull(started.questions().get(0).correctAnswer(), "the answer key is hidden while in progress");

        String id = started.id().toString();
        service.answerQuestion(new AnswerQuestionCommand(id, 10L, "B"));
        service.answerQuestion(new AnswerQuestionCommand(id, 11L, "TRUE"));
        AttemptResponse result = service.submitAttempt(id);

        assertEquals("SUBMITTED", result.status());
        assertEquals(1.0, result.score().earnedPoints());
        assertEquals(33.3, result.score().percentage());
        assertEquals("FALSE", result.questions().get(1).correctAnswer());
        assertEquals(1, service.listAttemptsOfStudent(5L).size());
        assertEquals(List.of(AssessmentEvent.ATTEMPT_STARTED, AssessmentEvent.ATTEMPT_SUBMITTED),
                published.stream().map(AssessmentEvent::eventType).toList());
        assertEquals(33.3, published.get(1).percentage());
    }

    @Test
    void nonStudentsAndUnknownResourcesAreRejected() {
        assertThrows(AssessmentRuleException.class, () -> service.startAttempt(new StartAttemptCommand(1L, 4L)));
        assertThrows(AssessmentNotFoundException.class, () -> service.startAttempt(new StartAttemptCommand(1L, 77L)));
        assertThrows(AssessmentNotFoundException.class, () -> service.startAttempt(new StartAttemptCommand(9L, 5L)));
    }

    @Test
    void expiredAttemptIsClosedSoTheStudentCanStartAgain() {
        service.startAttempt(new StartAttemptCommand(1L, 5L));
        assertThrows(AssessmentRuleException.class, () -> service.startAttempt(new StartAttemptCommand(1L, 5L)));

        clock.now = clock.now.plusHours(1);
        AttemptResponse second = service.startAttempt(new StartAttemptCommand(1L, 5L));
        assertEquals("IN_PROGRESS", second.status());
        assertEquals("SUBMITTED", service.listAttemptsOfStudent(5L).get(1).status());
        assertEquals(3, published.size(), "started, closed by time, started again");
    }
}
