package com.unicauca.lisw2t2g08.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la maquina de estados del ciclo de vida de una pregunta (HU04-02).
 */
class QuestionStateTest {

    @Test
    @DisplayName("El ciclo de vida define las transiciones esperadas")
    void shouldDefineExpectedTransitions() {
        assertEquals(EnumSet.of(QuestionState.PENDIENTE_REVISION),
                QuestionState.BORRADOR.allowedTransitions());
        assertEquals(EnumSet.of(QuestionState.EN_REVISION, QuestionState.BORRADOR),
                QuestionState.PENDIENTE_REVISION.allowedTransitions());
        assertEquals(EnumSet.of(QuestionState.APROBADA, QuestionState.RECHAZADA),
                QuestionState.EN_REVISION.allowedTransitions());
        assertEquals(EnumSet.of(QuestionState.PUBLICADA, QuestionState.ARCHIVADA),
                QuestionState.APROBADA.allowedTransitions());
        assertEquals(EnumSet.of(QuestionState.ARCHIVADA),
                QuestionState.PUBLICADA.allowedTransitions());
    }

    @Test
    @DisplayName("Archivada es un estado final")
    void shouldTreatArchivadaAsFinal() {
        assertTrue(QuestionState.ARCHIVADA.isFinal());
        assertTrue(QuestionState.ARCHIVADA.allowedTransitions().isEmpty());
        assertFalse(QuestionState.BORRADOR.isFinal());
    }

    @Test
    @DisplayName("canTransitionTo refleja las transiciones validas")
    void shouldReportValidTransitions() {
        assertTrue(QuestionState.BORRADOR.canTransitionTo(QuestionState.PENDIENTE_REVISION));
        assertFalse(QuestionState.BORRADOR.canTransitionTo(QuestionState.PUBLICADA));
        assertFalse(QuestionState.BORRADOR.canTransitionTo(null));
        assertFalse(QuestionState.ARCHIVADA.canTransitionTo(QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("Los estados seleccionables incluyen el actual y sus transiciones")
    void shouldExposeSelectableTransitions() {
        Set<QuestionState> selectable = QuestionState.BORRADOR.selectableTransitions();

        assertTrue(selectable.contains(QuestionState.BORRADOR));
        assertTrue(selectable.contains(QuestionState.PENDIENTE_REVISION));
        assertEquals(2, selectable.size());
        assertEquals(Set.of(QuestionState.ARCHIVADA),
                QuestionState.ARCHIVADA.selectableTransitions());
    }

    @Test
    @DisplayName("El banco define los siete estados del ciclo de vida")
    void shouldDefineSevenStates() {
        assertEquals(7, QuestionState.values().length);
    }
}
