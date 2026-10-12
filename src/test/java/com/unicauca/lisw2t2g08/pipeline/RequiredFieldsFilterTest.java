package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequiredFieldsFilterTest {

    private final RequiredFieldsFilter filter = new RequiredFieldsFilter();

    @Test
    void aceptaUnBorradorCompleto() {
        QuestionDraft draft = DraftFactory.valid();
        assertSame(draft, filter.process(draft));
    }

    @Test
    void reportaTodosLosCamposFaltantes() {
        QuestionDraft draft = DraftFactory.valid();
        draft.setName(" ");
        draft.setJustification(null);
        draft.setBibliography("");
        draft.setCompetence(null);
        draft.setTopic(null);
        draft.setSubtopic(null);
        draft.setDifficulty(null);
        ValidationException ex = assertThrows(ValidationException.class, () -> filter.process(draft));
        assertEquals(7, ex.getErrors().size());
        assertTrue(ex.getErrors().stream().allMatch(e -> e.startsWith("RF-05")));
    }
}
