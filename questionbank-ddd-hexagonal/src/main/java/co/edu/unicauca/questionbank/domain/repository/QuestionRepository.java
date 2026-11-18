package co.edu.unicauca.questionbank.domain.repository;

import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;

import java.util.List;
import java.util.Optional;

/** Persistence contract for questions, expressed without Spring or JPA types. */
public interface QuestionRepository {

    /**
     * Stores a new question or updates an existing one, including its options,
     * metadata and classification.
     *
     * @param question question to store
     * @return the stored question, with its generated identities
     */
    Question save(Question question);

    /**
     * @param id question identifier
     * @return the question, if it exists
     */
    Optional<Question> findById(Long id);

    /** @return every question */
    List<Question> findAll();

    /**
     * @param authorId author identifier
     * @return questions written by the author
     */
    List<Question> findByAuthor(Long authorId);

    /**
     * @param status lifecycle status
     * @return questions currently in that status
     */
    List<Question> findByStatus(QuestionStatus status);

    /**
     * @param competencyId competency identifier
     * @return published questions that assess the competency
     */
    List<Question> findPublishedByCompetency(Long competencyId);

    /**
     * Removes a question together with its options and metadata.
     *
     * @param id question identifier
     */
    void deleteById(Long id);
}
