package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.List;

/**
 * RF-11: debe existir unicamente una respuesta correcta, indicada por un
 * indice valido sobre las opciones.
 */
public class CorrectAnswerFilter implements Filter {

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        List<String> options = draft.getOptions();
        int index = draft.getCorrectIndex();

        if (index < 0) {
            throw new ValidationException(List.of(
                    "RF-11: debe indicarse cual opcion es la respuesta correcta"));
        }
        if (index >= options.size()) {
            throw new ValidationException(List.of(
                    "RF-11: el indice de la respuesta correcta esta fuera de rango"));
        }

        String correct = options.get(index);
        if (correct == null || correct.isBlank()) {
            throw new ValidationException(List.of(
                    "RF-11: la respuesta correcta no puede estar vacia"));
        }
        for (int i = 0; i < options.size(); i++) {
            String other = options.get(i);
            if (i != index && other != null && other.trim().equalsIgnoreCase(correct.trim())) {
                throw new ValidationException(List.of(
                        "RF-11: debe existir una unica respuesta correcta, pero la opcion "
                                + (i + 1) + " repite la correcta"));
            }
        }
        return draft;
    }
}
