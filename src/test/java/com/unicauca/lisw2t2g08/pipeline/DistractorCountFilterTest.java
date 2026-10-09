package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistractorCountFilterTest {

    private final DistractorCountFilter filter = new DistractorCountFilter();

    @Test
    void aceptaCuatroDistractoresMasLaCorrecta() {
        assertDoesNotThrow(() -> filter.process(DraftFactory.valid()));
    }

    @Test
    void rechazaMenosDistractores() {
        QuestionDraft draft = DraftFactory.withOptions(List.of("Correcta", "Uno", "Dos"), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> filter.process(draft));
        assertTrue(ex.getMessage().contains("encontrados: 2"));
    }

    @Test
    void rechazaMasDistractores() {
        QuestionDraft draft = DraftFactory.withOptions(
                List.of("Correcta", "Uno", "Dos", "Tres", "Cuatro", "Cinco"), 0);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaSinOpciones() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setOptions(null);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }
}
