package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectQuestionFilterTest {

    private final DirectQuestionFilter filter = new DirectQuestionFilter();

    private QuestionDraft withQuestion(String question) {
        QuestionDraft draft = DraftFactory.valid();
        draft.setDirectQuestion(question);
        return draft;
    }

    @Test
    void aceptaUnaUnicaPreguntaDirecta() {
        assertDoesNotThrow(() -> filter.process(withQuestion("Que es un patron de diseno?")));
    }

    @Test
    void rechazaPreguntaNulaOVacia() {
        assertThrows(ValidationException.class, () -> filter.process(withQuestion(null)));
        assertThrows(ValidationException.class, () -> filter.process(withQuestion("  ")));
    }

    @Test
    void rechazaPreguntaSinSignoDeInterrogacionFinal() {
        assertThrows(ValidationException.class,
                () -> filter.process(withQuestion("Explique el patron Pipeline")));
    }

    @Test
    void rechazaPreguntaDemasiadoCorta() {
        assertThrows(ValidationException.class, () -> filter.process(withQuestion("Que?")));
    }

    @Test
    void rechazaVariasPreguntasDirectas() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filter.process(withQuestion("Que es un filtro? Y que es una tuberia?")));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("unica")));
    }
}
