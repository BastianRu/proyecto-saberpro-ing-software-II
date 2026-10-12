package com.unicauca.lisw2t2g08.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de la entidad Question.
 */
class QuestionTest {

    private QuestionDistractors options() {
        return new QuestionDistractors(List.of("Opcion A", "Opcion B", "Opcion C"), 1);
    }

    private Question newQuestion(QuestionState state) {
        return new Question("P-001", "Pregunta sobre DDD",
                "¿Cual es el objetivo principal de DDD?", options(), state);
    }

    @Test
    @DisplayName("Una pregunta nueva sin estado queda en BORRADOR")
    void shouldDefaultToBorradorWhenStateIsNull() {
        Question question = newQuestion(null);

        assertEquals(QuestionState.BORRADOR, question.getState());
    }

    @Test
    @DisplayName("El id es obligatorio")
    void shouldRejectBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new Question("  ", "Nombre",
                "Enunciado", options(), QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Question("P-001", "",
                "Enunciado", options(), QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("El enunciado es obligatorio")
    void shouldRejectBlankStatement() {
        assertThrows(IllegalArgumentException.class, () -> new Question("P-001", "Nombre",
                "   ", options(), QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("Las opciones de respuesta son obligatorias")
    void shouldRejectNullDistractors() {
        assertThrows(IllegalArgumentException.class, () -> new Question("P-001", "Nombre",
                "Enunciado", null, QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("El cambio de estado de borrador a pendiente de revision es valido")
    void shouldChangeStateFromBorradorToPendiente() {
        Question question = newQuestion(QuestionState.BORRADOR);

        question.changeState(QuestionState.PENDIENTE_REVISION);

        assertEquals(QuestionState.PENDIENTE_REVISION, question.getState());
    }

    @Test
    @DisplayName("Una pregunta eliminada no puede volver a otro estado")
    void shouldRejectReactivatingDeletedQuestion() {
        Question question = newQuestion(QuestionState.ELIMINADA);

        assertThrows(IllegalStateException.class,
                () -> question.changeState(QuestionState.BORRADOR));
        assertEquals(QuestionState.ELIMINADA, question.getState());
    }

    @Test
    @DisplayName("El nuevo estado no puede ser nulo")
    void shouldRejectNullState() {
        Question question = newQuestion(QuestionState.BORRADOR);

        assertThrows(IllegalArgumentException.class, () -> question.changeState(null));
    }

    @Test
    @DisplayName("La respuesta correcta se delega a las opciones")
    void shouldExposeCorrectAnswer() {
        Question question = newQuestion(QuestionState.BORRADOR);

        assertEquals("Opcion B", question.getCorrectAnswer());
    }

    @Test
    @DisplayName("Dos preguntas son iguales si comparten el id")
    void shouldCompareById() {
        Question one = newQuestion(QuestionState.BORRADOR);
        Question same = new Question("P-001", "Otro nombre", "Otro enunciado",
                options(), QuestionState.ELIMINADA);
        Question other = new Question("P-002", "Nombre", "Enunciado",
                options(), QuestionState.BORRADOR);

        assertEquals(one, same);
        assertEquals(one.hashCode(), same.hashCode());
        assertNotEquals(one, other);
    }

    @Test
    void modifyContentReemplazaLosCamposEnBorrador() {
        Question question = newQuestion(QuestionState.BORRADOR);
        question.modifyContent("Nuevo nombre", "Nuevo contexto", "¿Nueva pregunta?",
                new QuestionDistractors(List.of("Uno", "Dos"), 1), "Justif", "Biblio",
                "Competencia", "Tema", "Subtema", DifficultyLevel.ALTA);

        assertEquals("Nuevo nombre", question.getName());
        assertEquals("Nuevo contexto", question.getContext());
        assertEquals("¿Nueva pregunta?", question.getStatement());
        assertEquals("Dos", question.getCorrectAnswer());
        assertEquals("Subtema", question.getSubtopic());
        assertEquals(DifficultyLevel.ALTA, question.getDifficulty());
        assertEquals(QuestionState.BORRADOR, question.getState());
    }

    @Test
    void modifyContentSeRechazaFueraDeBorrador() {
        Question question = newQuestion(QuestionState.BORRADOR);
        question.changeState(QuestionState.PENDIENTE_REVISION);
        assertThrows(IllegalStateException.class, () -> question.modifyContent("N", "C", "¿P?",
                new QuestionDistractors(List.of("Uno", "Dos"), 0), "J", "B", "C", "T", "S",
                DifficultyLevel.BAJA));
    }

    @Test
    void modifyContentInvalidoNoDejaLaPreguntaAMedias() {
        Question question = newQuestion(QuestionState.BORRADOR);
        String name = question.getName();
        assertThrows(IllegalArgumentException.class, () -> question.modifyContent("Otro", "C", " ",
                new QuestionDistractors(List.of("Uno", "Dos"), 0), "J", "B", "C", "T", "S",
                DifficultyLevel.BAJA));
        assertEquals(name, question.getName());
    }
}
