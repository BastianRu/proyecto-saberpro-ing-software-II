package co.edu.unicauca.questionbank.domain.repository;

import co.edu.unicauca.questionbank.domain.model.Quiz;

import java.util.List;
import java.util.Optional;

/** Persistence contract for quizzes. */
public interface QuizRepository {

    /**
     * Stores a new quiz with its questions. Quizzes are not modified once created.
     *
     * @param quiz quiz to store
     * @return the stored quiz with its identity
     */
    Quiz save(Quiz quiz);

    /**
     * @param id quiz identifier
     * @return the quiz, if it exists
     */
    Optional<Quiz> findById(Long id);

    /** @return every quiz */
    List<Quiz> findAll();
}
