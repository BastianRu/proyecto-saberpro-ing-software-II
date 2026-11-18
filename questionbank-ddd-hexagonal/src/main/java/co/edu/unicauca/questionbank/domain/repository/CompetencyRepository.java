package co.edu.unicauca.questionbank.domain.repository;

import co.edu.unicauca.questionbank.domain.model.Competency;

import java.util.List;
import java.util.Optional;

/** Persistence contract for competencies. */
public interface CompetencyRepository {

    /**
     * @param competency competency to store
     * @return the stored competency with its identity
     */
    Competency save(Competency competency);

    /**
     * @param id competency identifier
     * @return the competency, if it exists
     */
    Optional<Competency> findById(Long id);

    /** @return every competency */
    List<Competency> findAll();
}
