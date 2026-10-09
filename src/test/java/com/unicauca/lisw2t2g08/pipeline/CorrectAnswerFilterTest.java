package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorrectAnswerFilterTest {

    private final CorrectAnswerFilter filter = new CorrectAnswerFilter();

    @Test
    void aceptaIndiceCorrectoValido() {
        assertDoesNotThrow(() -> filter.process(DraftFactory.valid()));
    }

    @Test
    void rechazaSinRespuestaCorrectaIndicada() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setCorrectIndex(-1);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaIndiceFueraDeRango() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setCorrectIndex(5);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaRespuestaCorrectaVacia() {
        QuestionDraft draft = DraftFactory.withOptions(
                Arrays.asList("  ", "Uno", "Dos", "Tres", "Cuatro"), 0);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaCuandoOtraOpcionRepiteLaCorrecta() {
        QuestionDraft draft = DraftFactory.withOptions(
                List.of("Correcta", "Uno", "correcta", "Tres", "Cuatro"), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> filter.process(draft));
        assertTrue(ex.getMessage().contains("unica respuesta correcta"));
    }
}
