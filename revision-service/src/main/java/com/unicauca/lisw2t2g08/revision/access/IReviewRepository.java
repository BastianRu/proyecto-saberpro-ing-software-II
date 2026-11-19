package com.unicauca.lisw2t2g08.revision.access;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.revision.domain.Review;
import com.unicauca.lisw2t2g08.revision.domain.ReviewStatus;

public interface IReviewRepository {

    /**
     * Guarda la revisión y marca el evento como procesado en una sola transacción, solo si ese
     * evento no se había procesado antes (entrega duplicada del broker).
     *
     * @return true si se creó; false si el evento ya estaba procesado.
     */
    boolean createFromEvent(String eventId, Review review);

    Optional<Review> findById(long id);

    /** @param status filtro opcional; null devuelve todas. */
    List<Review> findByStatus(ReviewStatus status);

    List<Review> findByQuestionId(String questionId);

    void update(Review review);

    /** Cancela las revisiones pendientes de la pregunta; @return cuántas canceló. */
    int cancelPendingByQuestion(String questionId, Instant now);
}
