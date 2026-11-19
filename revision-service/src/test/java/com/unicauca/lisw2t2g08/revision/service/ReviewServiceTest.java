package com.unicauca.lisw2t2g08.revision.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.revision.access.SQLiteReviewRepository;
import com.unicauca.lisw2t2g08.revision.config.ReviewConfig;
import com.unicauca.lisw2t2g08.revision.domain.ForbiddenOperationException;
import com.unicauca.lisw2t2g08.revision.domain.InvalidReviewOperationException;
import com.unicauca.lisw2t2g08.revision.domain.Review;
import com.unicauca.lisw2t2g08.revision.domain.ReviewDecision;
import com.unicauca.lisw2t2g08.revision.domain.ReviewStatus;
import com.unicauca.lisw2t2g08.revision.events.QuestionStateMessage;
import com.unicauca.lisw2t2g08.revision.security.AuthenticatedUser;

/** SQLite real en memoria (la idempotencia depende de la base) y el Banco reemplazado por un doble. */
class ReviewServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:00:00Z");

    private Connection connection;
    private FakeBancoClient banco;
    private ReviewService service;

    private final AuthenticatedUser beto = new AuthenticatedUser("beto", "REVISOR", "token-beto");
    private final AuthenticatedUser ana = new AuthenticatedUser("ana", "REVISOR", "token-ana");
    private final AuthenticatedUser docente = new AuthenticatedUser("doc", "DOCENTE", "token-doc");

    @BeforeEach
    void setUp() throws Exception {
        connection = new ReviewConfig().connection("jdbc:sqlite::memory:");
        banco = new FakeBancoClient();
        service = new ReviewService(new SQLiteReviewRepository(connection), banco,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private static QuestionStateMessage event(String id, String question, String from, String to, String user) {
        return new QuestionStateMessage(id, question, from, to, user, NOW);
    }

    private Review submitted(String question) {
        service.onQuestionStateChanged(event("ev-" + question, question, "BORRADOR", "PENDIENTE_REVISION", "ana"));
        return service.historyOf(question).get(0);
    }

    @Test
    void unaPreguntaEnviadaARevisionCreaUnaRevisionPendiente() {
        Review review = submitted("P-001");

        assertEquals(ReviewStatus.PENDIENTE, review.getStatus());
        assertEquals("ana", review.getRequestedBy());
    }

    @Test
    void elMismoEventoEntregadoDosVecesCreaUnaSolaRevision() {
        QuestionStateMessage e = event("dup", "P-001", "BORRADOR", "PENDIENTE_REVISION", "ana");
        service.onQuestionStateChanged(e);
        service.onQuestionStateChanged(e);

        assertEquals(1, service.list(null).size());
    }

    @Test
    void otrosCambiosDeEstadoSeIgnoran() {
        service.onQuestionStateChanged(event("e1", "P-001", "PENDIENTE_REVISION", "EN_REVISION", "beto"));
        service.onQuestionStateChanged(event("e2", "P-001", "EN_REVISION", "APROBADA", "beto"));
        assertTrue(service.list(null).isEmpty());
    }

    @Test
    void siElAutorRetiraLaPreguntaSeCancelaLaRevisionPendiente() {
        submitted("P-001");

        service.onQuestionStateChanged(event("e2", "P-001", "PENDIENTE_REVISION", "BORRADOR", "ana"));

        assertEquals(ReviewStatus.CANCELADA, service.historyOf("P-001").get(0).getStatus());
    }

    @Test
    void tomarPideAlBancoEnRevisionConElTokenDelRevisorYLaDejaEnCurso() {
        Review review = submitted("P-001");

        Review taken = service.take(review.getId(), beto);

        assertEquals(ReviewStatus.EN_CURSO, taken.getStatus());
        assertEquals(new FakeBancoClient.Call("P-001", "EN_REVISION", "token-beto"), banco.calls.get(0));
        assertEquals(ReviewStatus.EN_CURSO, service.get(review.getId()).getStatus()); // persistido
    }

    @Test
    void quienNoEsRevisorNoPuedeTomar() {
        Review review = submitted("P-001");
        assertThrows(ForbiddenOperationException.class, () -> service.take(review.getId(), docente));
        assertTrue(banco.calls.isEmpty());
    }

    @Test
    void elAutorNoPuedeTomarSuPropiaPreguntaYElBancoNoSeEntera() {
        Review review = submitted("P-001");
        assertThrows(ForbiddenOperationException.class, () -> service.take(review.getId(), ana));
        assertTrue(banco.calls.isEmpty());
    }

    @Test
    void siElBancoRechazaLaRevisionSigueEnPendiente() {
        Review review = submitted("P-001");
        banco.failWith = new BancoRejectedException("Transicion no permitida de Borrador a En revisión");

        assertThrows(BancoRejectedException.class, () -> service.take(review.getId(), beto));

        assertEquals(ReviewStatus.PENDIENTE, service.get(review.getId()).getStatus());
    }

    @Test
    void siElBancoEstaCaidoLaRevisionSigueEnPendiente() {
        Review review = submitted("P-001");
        banco.failWith = new BancoUnavailableException("caído", null);

        assertThrows(BancoUnavailableException.class, () -> service.take(review.getId(), beto));

        assertEquals(ReviewStatus.PENDIENTE, service.get(review.getId()).getStatus());
    }

    @Test
    void aprobarPideAprobadaAlBancoYCompleta() {
        Review review = submitted("P-001");
        service.take(review.getId(), beto);

        Review done = service.submitVerdict(review.getId(), beto, ReviewDecision.APROBAR, null);

        assertEquals(ReviewStatus.COMPLETADA, done.getStatus());
        assertEquals("APROBADA", banco.calls.get(1).targetState());
    }

    @Test
    void rechazarSinObservacionesNoLlegaAlBanco() {
        Review review = submitted("P-001");
        service.take(review.getId(), beto);
        banco.calls.clear();

        assertThrows(IllegalArgumentException.class,
                () -> service.submitVerdict(review.getId(), beto, ReviewDecision.RECHAZAR, ""));

        assertTrue(banco.calls.isEmpty());
        assertEquals(ReviewStatus.EN_CURSO, service.get(review.getId()).getStatus());
    }

    @Test
    void otroRevisorNoPuedeEmitirElDictamen() {
        Review review = submitted("P-001");
        service.take(review.getId(), beto);
        AuthenticatedUser carla = new AuthenticatedUser("carla", "REVISOR", "t");

        assertThrows(ForbiddenOperationException.class,
                () -> service.submitVerdict(review.getId(), carla, ReviewDecision.APROBAR, null));
    }

    @Test
    void noSeEmiteDictamenSobreUnaRevisionPendiente() {
        Review review = submitted("P-001");
        assertThrows(InvalidReviewOperationException.class,
                () -> service.submitVerdict(review.getId(), beto, ReviewDecision.APROBAR, null));
    }

    @Test
    void filtraPorEstado() {
        Review first = submitted("P-001");
        submitted("P-002");
        service.take(first.getId(), beto);

        assertEquals(1, service.list(ReviewStatus.PENDIENTE).size());
        assertEquals(1, service.list(ReviewStatus.EN_CURSO).size());
        assertEquals(2, service.list(null).size());
    }
}
