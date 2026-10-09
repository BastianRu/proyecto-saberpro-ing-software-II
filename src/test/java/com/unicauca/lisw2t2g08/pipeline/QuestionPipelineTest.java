package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionPipelineTest {

    private final QuestionPipeline pipeline = new QuestionPipeline();

    @Test
    void procesaUnBorradorCompletamenteValido() {
        QuestionDraft draft = DraftFactory.valid();
        assertSame(draft, pipeline.run(draft));
    }

    @Test
    void seDetieneEnElPrimerFiltroQueFalla() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setContext(null);
        draft.setOptions(List.of("Una sola"));
        ValidationException ex = assertThrows(ValidationException.class, () -> pipeline.run(draft));
        assertTrue(ex.getErrors().get(0).startsWith("RF-08"));
    }

    @Test
    void reportaRf10SiFaltanDistractores() {
        QuestionDraft draft = DraftFactory.withOptions(List.of("Correcta", "Uno"), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> pipeline.run(draft));
        assertTrue(ex.getErrors().get(0).startsWith("RF-10"));
    }

    @Test
    void reportaRf12ParaExpresionesProhibidas() {
        QuestionDraft draft = DraftFactory.withOptions(
                List.of("Correcta", "Uno", "Dos", "Tres", "Todas las anteriores"), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> pipeline.run(draft));
        assertTrue(ex.getErrors().get(0).startsWith("RF-12"));
    }

    @Test
    void reportaRf11SiNoHayRespuestaCorrecta() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setCorrectIndex(-1);
        ValidationException ex = assertThrows(ValidationException.class, () -> pipeline.run(draft));
        assertTrue(ex.getErrors().get(0).startsWith("RF-11"));
    }

    @Test
    void permiteTuberiasPersonalizadas() {
        QuestionPipeline custom = new QuestionPipeline(List.of(new ContextFilter()));
        QuestionDraft draft = DraftFactory.withOptions(List.of("x"), -1);
        assertDoesNotThrow(() -> custom.run(draft));
    }
}
