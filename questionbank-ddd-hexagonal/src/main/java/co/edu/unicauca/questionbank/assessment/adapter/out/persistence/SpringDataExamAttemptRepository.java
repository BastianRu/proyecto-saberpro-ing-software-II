package co.edu.unicauca.questionbank.assessment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data repository used internally by the attempt persistence adapter. It is not a port. */
public interface SpringDataExamAttemptRepository extends JpaRepository<ExamAttemptJpaEntity, UUID> {

    /**
     * @param studentId student identifier
     * @param quizId    quiz identifier
     * @return attempts of the student at the quiz
     */
    List<ExamAttemptJpaEntity> findByStudentIdAndQuizId(Long studentId, Long quizId);

    /**
     * @param studentId student identifier
     * @return attempts of the student, newest first
     */
    List<ExamAttemptJpaEntity> findByStudentIdOrderByStartedAtDesc(Long studentId);
}
