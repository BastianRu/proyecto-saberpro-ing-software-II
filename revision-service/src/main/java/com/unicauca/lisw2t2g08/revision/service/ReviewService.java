package com.unicauca.lisw2t2g08.revision.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unicauca.lisw2t2g08.revision.access.IReviewRepository;
import com.unicauca.lisw2t2g08.revision.domain.ForbiddenOperationException;
import com.unicauca.lisw2t2g08.revision.domain.Review;
import com.unicauca.lisw2t2g08.revision.domain.ReviewDecision;
import com.unicauca.lisw2t2g08.revision.domain.ReviewNotFoundException;
import com.unicauca.lisw2t2g08.revision.domain.ReviewStatus;
import com.unicauca.lisw2t2g08.revision.events.QuestionStateMessage;
import com.unicauca.lisw2t2g08.revision.security.AuthenticatedUser;

/**
 * Casos de uso de Revisión (HU05). Reacciona a los eventos del Banco y, cuando un revisor actúa,
 * pide al Banco la transición de estado correspondiente. El Banco sigue siendo el dueño del ciclo
 * de vida de la pregunta: este servicio nunca cambia un estado por su cuenta.
 */
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final String PENDING_REVIEW = "PENDIENTE_REVISION";
    private static final String DRAFT = "BORRADOR";
    private static final Set<String> REVIEWER_ROLES = Set.of("REVISOR", "ADMINISTRADOR");

    private final IReviewRepository repository;
    private final IBancoClient banco;
    private final Clock clock;

    public ReviewService(IReviewRepository repository, IBancoClient banco, Clock clock) {
        this.repository = repository;
        this.banco = banco;
        this.clock = clock;
    }

    /**
     * Reacción al evento QuestionStateChanged del Banco. Es idempotente.
     * <ul>
     *   <li>Pregunta enviada a revisión (-> PENDIENTE_REVISION): se crea una revisión pendiente.</li>
     *   <li>Pregunta retirada a Borrador mientras esperaba: se cancela su revisión pendiente.</li>
     *   <li>Cualquier otro cambio no le interesa a este servicio.</li>
     * </ul>
     */
    public void onQuestionStateChanged(QuestionStateMessage event) {
        Instant now = Instant.now(clock);
        if (PENDING_REVIEW.equals(event.newState())) {
            boolean created = repository.createFromEvent(event.eventId(),
                    Review.pending(event.questionId(), event.username(), event.occurredAt()));
            if (!created) {
                log.info("Evento duplicado descartado: {}", event.eventId());
            }
        } else if (PENDING_REVIEW.equals(event.previousState()) && DRAFT.equals(event.newState())) {
            repository.cancelPendingByQuestion(event.questionId(), now);
        }
    }

    public List<Review> list(ReviewStatus status) {
        return repository.findByStatus(status);
    }

    public Review get(long id) {
        return repository.findById(id).orElseThrow(() -> new ReviewNotFoundException("La revisión no existe: " + id));
    }

    public List<Review> historyOf(String questionId) {
        return repository.findByQuestionId(questionId);
    }

    /**
     * El revisor toma una revisión pendiente: la pregunta pasa a EN_REVISION en el Banco.
     * Orden deliberado: 1) se validan las reglas locales, 2) se llama al Banco (fuente de verdad del
     * estado de la pregunta), 3) se persiste. Así, si el Banco rechaza, no queda nada a medias aquí.
     * synchronized: evita que dos revisores tomen la misma revisión a la vez.
     */
    public synchronized Review take(long reviewId, AuthenticatedUser user) {
        requireReviewerRole(user);
        Review review = get(reviewId);
        review.assertCanBeTakenBy(user.login());

        banco.changeState(review.getQuestionId(), "EN_REVISION", user.token());

        review.take(user.login(), Instant.now(clock));
        repository.update(review);
        return review;
    }

    /** El revisor asignado emite el dictamen: la pregunta pasa a APROBADA o RECHAZADA en el Banco. */
    public synchronized Review submitVerdict(long reviewId, AuthenticatedUser user, ReviewDecision decision,
                                             String observations) {
        requireReviewerRole(user);
        Review review = get(reviewId);
        review.assertCanBeCompletedBy(user.login(), decision, observations);

        banco.changeState(review.getQuestionId(), decision.targetQuestionState(), user.token());

        review.complete(user.login(), decision, observations, Instant.now(clock));
        repository.update(review);
        return review;
    }

    // La autorización fina se profundiza en la iteración 3; aquí solo se exige un rol de revisión
    private static void requireReviewerRole(AuthenticatedUser user) {
        if (!REVIEWER_ROLES.contains(user.role())) {
            throw new ForbiddenOperationException("Solo un REVISOR o un ADMINISTRADOR puede revisar preguntas");
        }
    }
}
