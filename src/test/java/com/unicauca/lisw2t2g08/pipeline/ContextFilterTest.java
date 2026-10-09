package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextFilterTest {

    private final ContextFilter filter = new ContextFilter();

    @Test
    void aceptaContextoValido() {
        assertDoesNotThrow(() -> filter.process(DraftFactory.valid()));
    }

    @Test
    void rechazaContextoNulo() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setContext(null);
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaContextoEnBlanco() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setContext("    ");
        assertThrows(ValidationException.class, () -> filter.process(draft));
    }

    @Test
    void rechazaContextoDemasiadoCorto() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setContext("Muy corto");
        ValidationException ex = assertThrows(ValidationException.class, () -> filter.process(draft));
        assertTrue(ex.getErrors().get(0).startsWith("RF-08"));
    }
}
