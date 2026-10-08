package com.unicauca.lisw2t2g08.service;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.infra.Observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del servicio de dominio, incluido su rol de sujeto
 * observable del patron Observer.
 */
class QuestionServiceTest {

    /** Observador de prueba que solo registra lo que recibe. */
    private static class SpyObserver implements Observer {

        private int calls;
        private QuestionStatistics lastStatistics;

        @Override
        public void update(Object data) {
            calls++;
            if (data instanceof QuestionStatistics statistics) {
                lastStatistics = statistics;
            }
        }
    }

    private FakeQuestionRepository repository;
    private QuestionService service;

    @BeforeEach
    void setUp() {
        repository = new FakeQuestionRepository();
        service = new QuestionService(repository);
        repository.save(question("P-001", QuestionState.BORRADOR));
        repository.save(question("P-002", QuestionState.BORRADOR));
        repository.save(question("P-003", QuestionState.PENDIENTE_REVISION));
        repository.save(question("P-004", QuestionState.ELIMINADA));
    }

    private Question question(String id, QuestionState state) {
        return new Question(id, "Pregunta " + id, "Enunciado de " + id,
                new QuestionDistractors(List.of("Opcion A", "Opcion B"), 1), state);
    }

    @Test
    @DisplayName("El repositorio es obligatorio")
    void shouldRejectNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new QuestionService(null));
    }

    @Test
    @DisplayName("Lista todas las preguntas del banco")
    void shouldListQuestions() {
        assertEquals(4, service.listQuestions().size());
    }

    @Test
    @DisplayName("Busca una pregunta por id")
    void shouldFindQuestionById() {
        assertTrue(service.findById("P-001").isPresent());
        assertTrue(service.findById("P-999").isEmpty());
        assertTrue(service.findById(null).isEmpty());
        assertTrue(service.findById("  ").isEmpty());
    }

    @Test
    @DisplayName("Cuenta las preguntas que hay en cada estado")
    void shouldComputeStatistics() {
        QuestionStatistics stats = service.getStatistics();

        assertEquals(2, stats.getCount(QuestionState.BORRADOR));
        assertEquals(1, stats.getCount(QuestionState.PENDIENTE_REVISION));
        assertEquals(1, stats.getCount(QuestionState.ELIMINADA));
        assertEquals(4, stats.getTotal());
    }

    @Test
    @DisplayName("Cambiar el estado actualiza la pregunta en el repositorio")
    void shouldChangeState() {
        boolean updated = service.changeState("P-001", QuestionState.PENDIENTE_REVISION);

        assertTrue(updated);
        assertEquals(QuestionState.PENDIENTE_REVISION,
                repository.findById("P-001").orElseThrow().getState());
    }

    @Test
    @DisplayName("No cambia el estado de una pregunta inexistente")
    void shouldNotChangeStateOfUnknownQuestion() {
        assertFalse(service.changeState("P-999", QuestionState.ELIMINADA));
    }

    @Test
    @DisplayName("Propaga la regla que impide reactivar una pregunta eliminada")
    void shouldPropagateInvalidTransition() {
        assertThrows(IllegalStateException.class,
                () -> service.changeState("P-004", QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("Al cambiar el estado se notifica a todas las vistas observadoras")
    void shouldNotifyAllObserversOnStateChange() {
        SpyObserver first = new SpyObserver();
        SpyObserver second = new SpyObserver();
        service.addObserver(first);
        service.addObserver(second);

        service.changeState("P-001", QuestionState.ELIMINADA);

        assertEquals(1, first.calls);
        assertEquals(1, second.calls);
        assertEquals(1, first.lastStatistics.getCount(QuestionState.BORRADOR));
        assertEquals(2, second.lastStatistics.getCount(QuestionState.ELIMINADA));
    }

    @Test
    @DisplayName("Un cambio de estado fallido no notifica a los observadores")
    void shouldNotNotifyWhenStateDoesNotChange() {
        SpyObserver observer = new SpyObserver();
        service.addObserver(observer);

        service.changeState("P-999", QuestionState.ELIMINADA);

        assertEquals(0, observer.calls);
    }

    @Test
    @DisplayName("Registrar una pregunta nueva notifica a los observadores")
    void shouldNotifyOnRegister() {
        SpyObserver observer = new SpyObserver();
        service.addObserver(observer);

        assertTrue(service.registerQuestion(question("P-005", QuestionState.BORRADOR)));
        assertEquals(1, observer.calls);
        assertEquals(5, observer.lastStatistics.getTotal());
    }

    @Test
    @DisplayName("No registra una pregunta con un id ya existente")
    void shouldRejectDuplicatedQuestion() {
        SpyObserver observer = new SpyObserver();
        service.addObserver(observer);

        assertFalse(service.registerQuestion(question("P-001", QuestionState.BORRADOR)));
        assertEquals(0, observer.calls);
    }

    @Test
    @DisplayName("La pregunta a registrar es obligatoria")
    void shouldRejectNullQuestionOnRegister() {
        assertThrows(IllegalArgumentException.class, () -> service.registerQuestion(null));
    }
}
