package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistractorFormatFilterTest {

    private final DistractorFormatFilter filter = new DistractorFormatFilter();

    @Test
    void aceptaOpcionesBienFormadas() {
        assertDoesNotThrow(() -> filter.process(DraftFactory.valid()));
    }

    @Test
    void rechazaOpcionVacia() {
        QuestionDraft draft = DraftFactory.withOptions(
                Arrays.asList("Correcta", "", "Dos", "Tres", "Cuatro"), 0);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaOpcionDemasiadoCorta() {
        QuestionDraft draft = DraftFactory.withOptions(List.of("Correcta", "Si", "Dos", "Tres", "Cuatro"), 0);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaOpcionSinMayusculaInicial() {
        QuestionDraft draft = DraftFactory.withOptions(
                List.of("Correcta", "en minuscula", "Dos", "Tres", "Cuatro"), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> filter.process(draft));
        assertTrue(ex.getErrors().get(0).contains("mayuscula"));
    }

    @Test
    void rechazaOpcionesDuplicadas() {
        QuestionDraft draft = DraftFactory.withOptions(
                List.of("Correcta", "Repetida", "Dos", "Tres", "repetida"), 0);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void acumulaVariosErroresEnUnaSolaExcepcion() {
        QuestionDraft draft = DraftFactory.withOptions(
                List.of("correcta", "no", "Dos", "Tres", "Cuatro"), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> filter.process(draft));
        assertTrue(ex.getErrors().size() >= 3);
    }
}
