package co.edu.unicauca.questionbank.assessment.adapter.out.persistence;

import co.edu.unicauca.questionbank.assessment.application.port.out.ExamAttemptRepositoryPort;
import co.edu.unicauca.questionbank.assessment.domain.model.ExamAttempt;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.AttemptId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Secondary (driven) adapter: implements the attempt repository port with Spring Data JPA and H2. */
@Repository
public class ExamAttemptRepositoryAdapter implements ExamAttemptRepositoryPort {

    private final SpringDataExamAttemptRepository repository;

    /** @param repository Spring Data implementation */
    public ExamAttemptRepositoryAdapter(SpringDataExamAttemptRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExamAttempt save(ExamAttempt attempt) {
        return repository.save(ExamAttemptJpaEntity.fromDomain(attempt)).toDomain();
    }

    @Override
    public Optional<ExamAttempt> findById(AttemptId id) {
        return repository.findById(id.value()).map(ExamAttemptJpaEntity::toDomain);
    }

    @Override
    public List<ExamAttempt> findByStudentAndQuiz(StudentId studentId, QuizId quizId) {
        return repository.findByStudentIdAndQuizId(studentId.value(), quizId.value()).stream()
                .map(ExamAttemptJpaEntity::toDomain).toList();
    }

    @Override
    public List<ExamAttempt> findByStudent(StudentId studentId) {
        return repository.findByStudentIdOrderByStartedAtDesc(studentId.value()).stream()
                .map(ExamAttemptJpaEntity::toDomain).toList();
    }
}
