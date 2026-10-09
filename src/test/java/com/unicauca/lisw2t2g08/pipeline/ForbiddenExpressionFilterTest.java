package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenExpressionFilterTest {

    private final ForbiddenExpressionFilter filter = new ForbiddenExpressionFilter();

    private QuestionDraft withLastOption(String option) {
        return DraftFactory.withOptions(List.of("Correcta", "Uno", "Dos", "Tres", option), 0);
    }

    @Test
    void aceptaOpcionesNormales() {
        assertDoesNotThrow(() -> filter.process(DraftFactory.valid()));
    }

    @Test
    void rechazaTodasLasAnteriores() {
        assertThrows(ValidationException.class, () -> filter.process(withLastOption("Todas las anteriores")));
    }

    @Test
    void rechazaNingunaDeLasAnterioresSinImportarMayusculasNiPuntoFinal() {
        assertThrows(ValidationException.class,
                () -> filter.process(withLastOption("  NINGUNA DE LAS ANTERIORES. ")));
    }

    @Test
    void ignoraOpcionesNulas() {
        QuestionDraft draft = DraftFactory.withOptions(
                Arrays.asList("Correcta", null, "Dos", "Tres", "Cuatro"), 0);
        assertDoesNotThrow(() -> filter.process(draft));
    }
}
