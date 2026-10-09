package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.List;

/**
 * RF-10: deben existir exactamente cuatro distractores, es decir cinco
 * opciones en total (la respuesta correcta mas cuatro distractores).
 */
public class DistractorCountFilter implements Filter {

    static final int REQUIRED_DISTRACTORS = 4;

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        int total = draft.getOptions().size();
        int distractors = Math.max(0, total - 1);
        if (total != REQUIRED_DISTRACTORS + 1) {
            throw new ValidationException(List.of(
                    "RF-10: se requieren exactamente " + REQUIRED_DISTRACTORS
                            + " distractores (encontrados: " + distractors + ")"));
        }
        return draft;
    }
}
