package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RF-13: las opciones deben respetar criterios basicos de longitud y
 * estructura gramatical: no vacias, longitud minima, inicio en mayuscula y
 * sin repetirse.
 */
public class DistractorFormatFilter implements Filter {

    private static final int MIN_OPTION_LENGTH = 3;

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        List<String> errors = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<String> options = draft.getOptions();

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if (option == null || option.isBlank()) {
                errors.add("RF-13: la opcion " + (i + 1) + " no puede estar vacia");
                continue;
            }
            String trimmed = option.trim();
            if (trimmed.length() < MIN_OPTION_LENGTH) {
                errors.add("RF-13: la opcion \"" + trimmed + "\" es demasiado corta");
            }
            if (!Character.isUpperCase(trimmed.codePointAt(0))) {
                errors.add("RF-13: la opcion \"" + trimmed + "\" debe iniciar con mayuscula");
            }
            if (!seen.add(trimmed.toLowerCase(Locale.ROOT))) {
                errors.add("RF-13: la opcion \"" + trimmed + "\" esta duplicada");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return draft;
    }
}
