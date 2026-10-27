package co.edu.unicauca.questionbank.domain.repository;

import co.edu.unicauca.questionbank.domain.model.Topic;

import java.util.List;
import java.util.Optional;

/** Persistence contract for topics. */
public interface TopicRepository {

    /**
     * @param topic topic to store
     * @return the stored topic with its identity
     */
    Topic save(Topic topic);

    /**
     * @param id topic identifier
     * @return the topic, if it exists
     */
    Optional<Topic> findById(Long id);

    /** @return every topic */
    List<Topic> findAll();
}
