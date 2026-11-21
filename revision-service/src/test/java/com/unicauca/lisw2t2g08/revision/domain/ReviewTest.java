package com.unicauca.lisw2t2g08.revision.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ReviewTest {

    private static final Instant T0 = Instant.parse("2026-11-19T10:00:00Z");

    private static Review pending() {
        return Review.pending("P-001", "ana", T0);
    }

    private static Review inProgress() {
        Review review = pending();
        review.take("beto", T0);
        return review;
    }

    @Test
    void unaRevisionNuevaEstaPendiente() {
        assertEquals(ReviewStatus.PENDIENTE, pending().getStatus());
    }

    @Test
    void unRevisorNoPuedeRevisarSuPropiaPregunta() {
        assertThrows(ForbiddenOperationException.class, () -> pending().take("ana", T0));
    }

    @Test
    void tomarRegistraAlRevisorYPasaAEnCurso() {
        Review review = inProgress();
        assertEquals(ReviewStatus.EN_CURSO, review.getStatus());
        assertEquals("beto", review.getReviewer());
    }

    @Test
    void noSePuedeTomarDosVeces() {
        assertThrows(InvalidReviewOperationException.class, () -> inProgress().take("carla", T0));
    }

    @Test
    void aprobarCompletaLaRevisionSinExigirObservaciones() {
        Review review = inProgress();
        review.complete("beto", ReviewDecision.APROBAR, null, T0);
        assertEquals(ReviewStatus.COMPLETADA, review.getStatus());
        assertEquals(ReviewDecision.APROBAR, review.getDecision());
    }

    @Test
    void rechazarExigeObservaciones() {
        assertThrows(IllegalArgumentException.class,
                () -> inProgress().complete("beto", ReviewDecision.RECHAZAR, "  ", T0));
        Review review = inProgress();
        review.complete("beto", ReviewDecision.RECHAZAR, " Falta la justificación ", T0);
        assertEquals("Falta la justificación", review.getObservations());
    }

    @Test
    void soloElRevisorAsignadoEmiteElDictamen() {
        assertThrows(ForbiddenOperationException.class,
                () -> inProgress().complete("carla", ReviewDecision.APROBAR, null, T0));
    }

    @Test
    void noSeEmiteDictamenSobreUnaRevisionPendienteOCompletada() {
        assertThrows(InvalidReviewOperationException.class,
                () -> pending().complete("beto", ReviewDecision.APROBAR, null, T0));
        Review done = inProgress();
        done.complete("beto", ReviewDecision.APROBAR, null, T0);
        assertThrows(InvalidReviewOperationException.class,
                () -> done.complete("beto", ReviewDecision.APROBAR, null, T0));
    }

    @Test
    void sinDictamenEsArgumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> inProgress().complete("beto", null, null, T0));
    }

    @Test
    void soloSeCancelaUnaRevisionPendiente() {
        Review review = pending();
        review.cancel(T0);
        assertEquals(ReviewStatus.CANCELADA, review.getStatus());
        assertThrows(InvalidReviewOperationException.class, () -> inProgress().cancel(T0));
    }

    @Test
    void cadaDictamenApuntaAlEstadoCorrectoDelBanco() {
        assertEquals("APROBADA", ReviewDecision.APROBAR.targetQuestionState());
        assertEquals("RECHAZADA", ReviewDecision.RECHAZAR.targetQuestionState());
    }
}
