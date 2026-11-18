package co.edu.unicauca.questionbank.infrastructure.jpa.repository;

import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data interface used internally by the question repository
 * implementation. Queries are polymorphic: they return both multiple choice
 * and true/false records.
 */
public interface SpringQuestionDataRepository extends JpaRepository<QuestionRecord, Long> {

    /**
     * @param authorId author identifier
     * @return questions of the author ordered by identity
     */
    List<QuestionRecord> findByAuthor_IdOrderByIdAsc(Long authorId);

    /**
     * @param status lifecycle status
     * @return questions in that status ordered by identity
     */
    List<QuestionRecord> findByStatusOrderByIdAsc(QuestionStatus status);

    /**
     * Traverses the many-to-many {@code question_competencies} join table.
     *
     * @param competencyId competency identifier
     * @param status       lifecycle status to filter
     * @return questions assessing the competency in the given status
     */
    @Query("""
            select distinct q from QuestionRecord q
            join q.competencies c
            where c.id = :competencyId and q.status = :status
            order by q.id
            """)
    List<QuestionRecord> findByCompetencyAndStatus(@Param("competencyId") Long competencyId,
                                                   @Param("status") QuestionStatus status);
}
