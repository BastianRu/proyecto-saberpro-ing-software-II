package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RF-12: impide expresiones como "Todas las anteriores" o "Ninguna de las
 * anteriores" en las opciones.
 */
public class ForbiddenExpressionFilter implements Filter {

    private static final Set<String> FORBIDDEN_PHRASES = Set.of(
            "todas las anteriores",
            "ninguna de las anteriores");

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        List<String> errors = new ArrayList<>();
        for (String option : draft.getOptions()) {
            if (option == null) {
                continue;
            }
            String normalized = option.trim().toLowerCase(Locale.ROOT).replaceAll("[.;,\\s]+$", "");
            if (FORBIDDEN_PHRASES.contains(normalized)) {
                errors.add("RF-12: no se permite la expresion \"" + option.trim() + "\"");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return draft;
    }
}
