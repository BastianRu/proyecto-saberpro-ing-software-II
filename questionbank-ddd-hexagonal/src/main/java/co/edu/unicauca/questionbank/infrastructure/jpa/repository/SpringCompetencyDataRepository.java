package co.edu.unicauca.questionbank.infrastructure.jpa.repository;

import co.edu.unicauca.questionbank.infrastructure.jpa.record.CompetencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data interface used internally by the competency repository implementation. */
public interface SpringCompetencyDataRepository extends JpaRepository<CompetencyRecord, Long> {}
