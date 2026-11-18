package co.edu.unicauca.questionbank.assessment.application.port.out;

import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;

import java.util.List;
import java.util.Optional;

/** Secondary (outbound) port to store ExamAttempt aggregates, answers included. */
public interface ExamAttemptRepositoryPort {

    /**
     * Stores the complete aggregate.
     *
     * @param attempt attempt to store
     * @return the stored attempt
     */
    ExamAttempt save(ExamAttempt attempt);

    /**
     * @param id attempt identity
     * @return the attempt, if it exists
     */
    Optional<ExamAttempt> findById(AttemptId id);

    /**
     * @param studentId student
     * @param quizId    quiz
     * @return every attempt of the student at the quiz
     */
    List<ExamAttempt> findByStudentAndQuiz(StudentId studentId, QuizId quizId);

    /**
     * @param studentId student
     * @return every attempt of the student, newest first
     */
    List<ExamAttempt> findByStudent(StudentId studentId);
}
