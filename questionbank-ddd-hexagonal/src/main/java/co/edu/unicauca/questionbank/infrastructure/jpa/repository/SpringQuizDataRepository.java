package co.edu.unicauca.questionbank.infrastructure.jpa.repository;

import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data interface used internally by the quiz repository implementation. */
public interface SpringQuizDataRepository extends JpaRepository<QuizRecord, Long> {}
