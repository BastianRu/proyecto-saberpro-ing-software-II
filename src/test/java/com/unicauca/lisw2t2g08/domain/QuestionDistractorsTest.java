package com.unicauca.lisw2t2g08.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del objeto de valor QuestionDistractors.
 */
class QuestionDistractorsTest {

    private static final List<String> OPTIONS = List.of(
            "Disenar bases de datos",
            "Modelar el dominio del negocio",
            "Eliminar UML",
            "Crear interfaces graficas");

    @Test
    @DisplayName("Expone la respuesta correcta y su letra")
    void shouldExposeCorrectAnswerAndLetter() {
        QuestionDistractors distractors = new QuestionDistractors(OPTIONS, 1);

        assertEquals("Modelar el dominio del negocio", distractors.getCorrectAnswer());
        assertEquals('B', distractors.getCorrectLetter());
    }

    @Test
    @DisplayName("Los distractores son las opciones distintas de la correcta")
    void shouldReturnOnlyIncorrectOptions() {
        QuestionDistractors distractors = new QuestionDistractors(OPTIONS, 1);

        assertEquals(List.of("Disenar bases de datos", "Eliminar UML", "Crear interfaces graficas"),
                distractors.getDistractors());
    }

    @Test
    @DisplayName("Rotula las opciones con letras consecutivas")
    void shouldLabelOptions() {
        QuestionDistractors distractors = new QuestionDistractors(OPTIONS, 0);

        assertEquals("A. Disenar bases de datos", distractors.getLabeledOption(0));
        assertEquals("D. Crear interfaces graficas", distractors.getLabeledOption(3));
    }

    @Test
    @DisplayName("Se requieren al menos dos opciones")
    void shouldRejectFewerThanTwoOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionDistractors(List.of("Unica opcion"), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionDistractors(null, 0));
    }

    @Test
    @DisplayName("Ninguna opcion puede estar vacia")
    void shouldRejectBlankOption() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionDistractors(Arrays.asList("Valida", "  "), 0));
    }

    @Test
    @DisplayName("El indice de la respuesta correcta debe existir")
    void shouldRejectIndexOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionDistractors(OPTIONS, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionDistractors(OPTIONS, -1));
    }

    @Test
    @DisplayName("Las opciones quedan protegidas contra modificaciones externas")
    void shouldCopyOptionsDefensively() {
        List<String> mutable = new ArrayList<>(OPTIONS);
        QuestionDistractors distractors = new QuestionDistractors(mutable, 1);

        mutable.clear();

        assertEquals(4, distractors.getOptions().size());
        assertThrows(UnsupportedOperationException.class,
                () -> distractors.getOptions().add("Nueva"));
    }
}
