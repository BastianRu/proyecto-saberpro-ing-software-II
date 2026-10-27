package co.edu.unicauca.questionbank.infrastructure.jpa.repository;

import co.edu.unicauca.questionbank.infrastructure.jpa.record.TopicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data interface used internally by the topic repository implementation. */
public interface SpringTopicDataRepository extends JpaRepository<TopicRecord, Long> {}
