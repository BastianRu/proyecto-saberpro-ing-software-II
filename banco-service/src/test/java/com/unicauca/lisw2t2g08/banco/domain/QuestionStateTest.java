package com.unicauca.lisw2t2g08.banco.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de la jerarquía de estados (patrón State) del ciclo de vida de una pregunta. */
class QuestionStateTest {

    private static Question questionIn(QuestionState state) {
        return new Question("P-001", "Nombre", "Contexto", "¿Pregunta?",
                new QuestionDistractors(List.of("Uno", "Dos", "Tres", "Cuatro", "Cinco"), 0),
                "", "", "", "", "", DifficultyLevel.MEDIA, state);
    }

    @Test
    @DisplayName("Los siete estados son subclases distintas de QuestionState")
    void sevenDistinctSubclasses() {
        assertEquals(7, QuestionStates.all().size());
        assertEquals(7, QuestionStates.all().stream().map(Object::getClass).distinct().count());
    }

    @Test
    @DisplayName("Cada estado define las transiciones esperadas del ciclo de vida")
    void expectedTransitions() {
        assertEquals(List.of(QuestionStates.PENDIENTE_REVISION), QuestionStates.BORRADOR.allowedTransitions());
        assertEquals(List.of(QuestionStates.EN_REVISION, QuestionStates.BORRADOR),
                QuestionStates.PENDIENTE_REVISION.allowedTransitions());
        assertEquals(List.of(QuestionStates.APROBADA, QuestionStates.RECHAZADA),
                QuestionStates.EN_REVISION.allowedTransitions());
        assertEquals(List.of(QuestionStates.PUBLICADA, QuestionStates.ARCHIVADA),
                QuestionStates.APROBADA.allowedTransitions());
        assertEquals(List.of(QuestionStates.BORRADOR, QuestionStates.ARCHIVADA),
                QuestionStates.RECHAZADA.allowedTransitions());
        assertEquals(List.of(QuestionStates.ARCHIVADA), QuestionStates.PUBLICADA.allowedTransitions());
        assertTrue(QuestionStates.ARCHIVADA.isFinal());
    }

    /**
     * Coherencia entre las dos fuentes de verdad: allowedTransitions() (consulta) y el
     * comportamiento real de changeState() (doble despacho). Recorre las 49 parejas de estados.
     */
    @Test
    @DisplayName("allowedTransitions coincide con lo que changeState realmente permite, para las 49 parejas")
    void allowedTransitionsMatchBehaviour() {
        for (QuestionState from : QuestionStates.all()) {
            for (QuestionState to : QuestionStates.all()) {
                Question question = questionIn(from);
                if (from == to || from.canTransitionTo(to)) {
                    question.changeState(to);
                    assertSame(to, question.getState(), from + " -> " + to);
                } else {
                    assertThrows(InvalidStateOperationException.class, () -> question.changeState(to),
                            from + " -> " + to);
                    assertSame(from, question.getState(), "el estado no debe cambiar tras una transición inválida");
                }
            }
        }
    }

    @Test
    @DisplayName("El mensaje de una transición inválida nombra el estado de origen y el de destino")
    void invalidTransitionMessage() {
        Question question = questionIn(QuestionStates.BORRADOR);
        InvalidStateOperationException e = assertThrows(InvalidStateOperationException.class,
                () -> question.changeState(QuestionStates.PUBLICADA));
        assertEquals("Transicion no permitida de Borrador a Publicada", e.getMessage());
    }

    @Test
    @DisplayName("Las operaciones de negocio recorren el flujo feliz completo")
    void happyPath() {
        Question question = questionIn(QuestionStates.BORRADOR);
        question.submitForReview();
        question.startReview();
        question.approve();
        question.publish();
        question.archive();
        assertSame(QuestionStates.ARCHIVADA, question.getState());
    }

    @Test
    @DisplayName("Una pregunta rechazada puede volver a Borrador y reenviarse")
    void rejectedGoesBackToDraft() {
        Question question = questionIn(QuestionStates.EN_REVISION);
        question.reject();
        question.returnToDraft();
        question.submitForReview();
        assertSame(QuestionStates.PENDIENTE_REVISION, question.getState());
    }

    @Test
    @DisplayName("Archivada no admite ninguna operación")
    void archivedIsTerminal() {
        Question question = questionIn(QuestionStates.ARCHIVADA);
        assertThrows(InvalidStateOperationException.class, question::returnToDraft);
        assertThrows(InvalidStateOperationException.class, question::submitForReview);
        assertThrows(InvalidStateOperationException.class, question::publish);
    }

    @Test
    @DisplayName("Solo Borrador permite modificar el contenido")
    void onlyDraftIsEditable() {
        for (QuestionState state : QuestionStates.all()) {
            assertEquals(state == QuestionStates.BORRADOR, state.isEditable(), state.getCode());
        }
        Question question = questionIn(QuestionStates.APROBADA);
        assertThrows(InvalidStateOperationException.class, () -> question.modifyContent("N", "C", "¿P?",
                new QuestionDistractors(List.of("Uno", "Dos"), 0), "", "", "", "", "", null));
    }

    @Test
    @DisplayName("fromCode resuelve los códigos y rechaza los desconocidos")
    void fromCode() {
        assertSame(QuestionStates.EN_REVISION, QuestionStates.fromCode("EN_REVISION"));
        assertThrows(IllegalArgumentException.class, () -> QuestionStates.fromCode("INVENTADO"));
        assertThrows(IllegalArgumentException.class, () -> QuestionStates.fromCode(null));
    }

    @Test
    @DisplayName("Cambiar a null es un error y cambiar al mismo estado no tiene efecto")
    void nullAndSameState() {
        Question question = questionIn(QuestionStates.BORRADOR);
        assertThrows(IllegalArgumentException.class, () -> question.changeState(null));
        question.changeState(QuestionStates.BORRADOR);
        assertFalse(question.getState().isFinal());
    }
}
