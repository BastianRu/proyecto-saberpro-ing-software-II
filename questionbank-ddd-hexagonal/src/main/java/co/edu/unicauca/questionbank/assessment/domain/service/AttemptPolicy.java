package co.edu.unicauca.questionbank.assessment.domain.service;

import co.edu.unicauca.questionbank.assessment.domain.exception.AssessmentRuleException;
import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentProfile;

import java.util.List;

/**
 * Domain service with the rule to start an attempt. The rule involves several
 * objects (the student, the exam and the previous attempts), so it does not
 * belong to a single aggregate. It contains no persistence or use-case logic.
 */
public class AttemptPolicy {

    /** Maximum number of attempts a student may make at the same quiz. */
    public static final int MAX_ATTEMPTS_PER_QUIZ = 3;

    /**
     * Validates that a student may start a new attempt.
     *
     * @param student          who wants to take the exam
     * @param sheet            exam to take
     * @param previousAttempts attempts of that student at the same quiz
     * @throws AssessmentRuleException if the student is not eligible
     */
    public void ensureCanStart(StudentProfile student, ExamSheet sheet, List<ExamAttempt> previousAttempts) {
        if (student == null || sheet == null || previousAttempts == null) {
            throw new IllegalArgumentException("Student, exam sheet and previous attempts are required");
        }
        if (!student.hasStudentRole()) {
            throw new AssessmentRuleException("User " + student.id() + " is not a STUDENT");
        }
        if (sheet.isEmpty()) {
            throw new AssessmentRuleException("Quiz " + sheet.quizId() + " has no questions");
        }
        if (previousAttempts.stream().anyMatch(ExamAttempt::isInProgress)) {
            throw new AssessmentRuleException("The student already has an attempt in progress for quiz "
                    + sheet.quizId());
        }
        if (previousAttempts.size() >= MAX_ATTEMPTS_PER_QUIZ) {
            throw new AssessmentRuleException("The student already used the " + MAX_ATTEMPTS_PER_QUIZ
                    + " attempts allowed for quiz " + sheet.quizId());
        }
    }
}
