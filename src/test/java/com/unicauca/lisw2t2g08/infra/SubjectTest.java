package com.unicauca.lisw2t2g08.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del sujeto observable de la capa transversal.
 */
class SubjectTest {

    /** Observador de prueba que acumula los datos recibidos. */
    private static class RecordingObserver implements Observer {

        private final List<Object> received = new ArrayList<>();

        @Override
        public void update(Object data) {
            received.add(data);
        }
    }

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
    }

    @Test
    @DisplayName("Registra observadores y los notifica a todos")
    void shouldNotifyAllRegisteredObservers() {
        RecordingObserver first = new RecordingObserver();
        RecordingObserver second = new RecordingObserver();
        subject.addObserver(first);
        subject.addObserver(second);

        subject.notifyAllObservers("cambio");

        assertEquals(2, subject.countObservers());
        assertEquals(List.of("cambio"), first.received);
        assertEquals(List.of("cambio"), second.received);
    }

    @Test
    @DisplayName("Ignora observadores nulos y duplicados")
    void shouldIgnoreNullAndDuplicatedObservers() {
        RecordingObserver observer = new RecordingObserver();

        subject.addObserver(null);
        subject.addObserver(observer);
        subject.addObserver(observer);

        assertEquals(1, subject.countObservers());
    }

    @Test
    @DisplayName("Un observador eliminado ya no recibe notificaciones")
    void shouldStopNotifyingRemovedObserver() {
        RecordingObserver observer = new RecordingObserver();
        subject.addObserver(observer);

        subject.removeObserver(observer);
        subject.notifyAllObservers("cambio");

        assertEquals(0, subject.countObservers());
        assertEquals(0, observer.received.size());
    }

    @Test
    @DisplayName("Notificar sin observadores no produce error")
    void shouldSupportNotifyingWithoutObservers() {
        subject.notifyAllObservers("cambio");

        assertEquals(0, subject.countObservers());
    }
}
