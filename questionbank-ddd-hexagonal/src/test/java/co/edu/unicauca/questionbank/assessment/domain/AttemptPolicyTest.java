package co.edu.unicauca.questionbank.assessment.domain;

import co.edu.unicauca.questionbank.assessment.domain.exception.AssessmentRuleException;
import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.service.AttemptPolicy;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentProfile;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Rules of the AttemptPolicy domain service. */
class AttemptPolicyTest {

    private final AttemptPolicy policy = new AttemptPolicy();
    private final ExamSheet sheet = ExamSheets.sample();
    private final StudentProfile student = new StudentProfile(StudentId.of(5L), "Juan Diaz", true);

    private ExamAttempt submittedAttempt() {
        ExamAttempt attempt = ExamAttempt.start(AttemptId.newId(), student.id(), sheet, LocalDateTime.now());
        attempt.submit(sheet, LocalDateTime.now());
        return attempt;
    }

    @Test
    void studentWithoutPreviousAttemptsCanStart() {
        assertDoesNotThrow(() -> policy.ensureCanStart(student, sheet, List.of()));
    }

    @Test
    void onlyStudentsCanTakeMockExams() {
        StudentProfile teacher = new StudentProfile(StudentId.of(4L), "Maria Ruiz", false);
        assertThrows(AssessmentRuleException.class, () -> policy.ensureCanStart(teacher, sheet, List.of()));
    }

    @Test
    void quizWithoutQuestionsCannotBeTaken() {
        assertThrows(AssessmentRuleException.class,
                () -> policy.ensureCanStart(student, ExamSheets.empty(), List.of()));
    }

    @Test
    void onlyOneAttemptInProgressPerQuiz() {
        ExamAttempt open = ExamAttempt.start(AttemptId.newId(), student.id(), sheet, LocalDateTime.now());
        assertThrows(AssessmentRuleException.class, () -> policy.ensureCanStart(student, sheet, List.of(open)));
    }

    @Test
    void attemptsPerQuizAreLimited() {
        List<ExamAttempt> previous = new ArrayList<>();
        for (int i = 0; i < AttemptPolicy.MAX_ATTEMPTS_PER_QUIZ - 1; i++) {
            previous.add(submittedAttempt());
        }
        assertDoesNotThrow(() -> policy.ensureCanStart(student, sheet, previous));
        previous.add(submittedAttempt());
        assertThrows(AssessmentRuleException.class, () -> policy.ensureCanStart(student, sheet, previous));
    }
}
