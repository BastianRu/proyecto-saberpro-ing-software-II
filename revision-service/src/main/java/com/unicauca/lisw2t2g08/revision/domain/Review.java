package com.unicauca.lisw2t2g08.revision.domain;

import java.time.Instant;

/**
 * Revisión por pares de una pregunta. Concentra las reglas de negocio de HU05:
 * <ul>
 *   <li>un revisor no puede revisar una pregunta que él mismo envió (es revisión "por pares");</li>
 *   <li>solo se toma una revisión pendiente y solo el revisor que la tomó emite el dictamen;</li>
 *   <li>rechazar exige observaciones, para que el autor sepa qué corregir.</li>
 * </ul>
 * Los métodos assertCan* validan sin modificar nada, para que el servicio pueda comprobar las reglas
 * ANTES de llamar al Banco y no deje la pregunta cambiada si la revisión no es válida.
 */
public class Review {

    private Long id;
    private final String questionId;
    private final String requestedBy;
    private final Instant requestedAt;
    private ReviewStatus status;
    private String reviewer;
    private Instant startedAt;
    private ReviewDecision decision;
    private String observations;
    private Instant completedAt;

    /** Revisión recién creada, pendiente de que un revisor la tome. */
    public static Review pending(String questionId, String requestedBy, Instant requestedAt) {
        return new Review(null, questionId, requestedBy, requestedAt, ReviewStatus.PENDIENTE,
                null, null, null, null, null);
    }

    /** Reconstruye una revisión ya persistida. */
    public Review(Long id, String questionId, String requestedBy, Instant requestedAt, ReviewStatus status,
                  String reviewer, Instant startedAt, ReviewDecision decision, String observations,
                  Instant completedAt) {
        if (questionId == null || questionId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la pregunta es obligatorio");
        }
        if (requestedAt == null || status == null) {
            throw new IllegalArgumentException("La fecha de solicitud y el estado son obligatorios");
        }
        this.id = id;
        this.questionId = questionId;
        this.requestedBy = (requestedBy == null || requestedBy.isBlank()) ? "sistema" : requestedBy;
        this.requestedAt = requestedAt;
        this.status = status;
        this.reviewer = reviewer;
        this.startedAt = startedAt;
        this.decision = decision;
        this.observations = observations;
        this.completedAt = completedAt;
    }

    /** @throws InvalidReviewOperationException si no está pendiente; ForbiddenOperationException si es el autor. */
    public void assertCanBeTakenBy(String login) {
        if (status != ReviewStatus.PENDIENTE) {
            throw new InvalidReviewOperationException(
                    "Solo se puede tomar una revisión pendiente (estado actual: " + status + ")");
        }
        if (requestedBy.equals(login)) {
            throw new ForbiddenOperationException("No se puede revisar una pregunta enviada por uno mismo");
        }
    }

    public void take(String login, Instant now) {
        assertCanBeTakenBy(login);
        this.status = ReviewStatus.EN_CURSO;
        this.reviewer = login;
        this.startedAt = now;
    }

    /** @throws InvalidReviewOperationException si no está en curso o falta la observación de un rechazo; ForbiddenOperationException si no es el revisor asignado. */
    public void assertCanBeCompletedBy(String login, ReviewDecision decision, String observations) {
        if (status != ReviewStatus.EN_CURSO) {
            throw new InvalidReviewOperationException(
                    "Solo se puede emitir dictamen sobre una revisión en curso (estado actual: " + status + ")");
        }
        if (!login.equals(reviewer)) {
            throw new ForbiddenOperationException("Solo el revisor que tomó la revisión puede emitir el dictamen");
        }
        if (decision == null) {
            throw new IllegalArgumentException("El dictamen es obligatorio (APROBAR o RECHAZAR)");
        }
        if (decision == ReviewDecision.RECHAZAR && (observations == null || observations.isBlank())) {
            throw new IllegalArgumentException("Un rechazo debe incluir observaciones para el autor");
        }
    }

    public void complete(String login, ReviewDecision decision, String observations, Instant now) {
        assertCanBeCompletedBy(login, decision, observations);
        this.status = ReviewStatus.COMPLETADA;
        this.decision = decision;
        this.observations = (observations == null || observations.isBlank()) ? null : observations.trim();
        this.completedAt = now;
    }

    /** El autor retiró la pregunta (volvió a Borrador): la revisión pendiente ya no tiene objeto. */
    public void cancel(Instant now) {
        if (status != ReviewStatus.PENDIENTE) {
            throw new InvalidReviewOperationException("Solo se puede cancelar una revisión pendiente");
        }
        this.status = ReviewStatus.CANCELADA;
        this.completedAt = now;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestionId() { return questionId; }
    public String getRequestedBy() { return requestedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public ReviewStatus getStatus() { return status; }
    public String getReviewer() { return reviewer; }
    public Instant getStartedAt() { return startedAt; }
    public ReviewDecision getDecision() { return decision; }
    public String getObservations() { return observations; }
    public Instant getCompletedAt() { return completedAt; }
}
