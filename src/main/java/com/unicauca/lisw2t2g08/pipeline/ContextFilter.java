package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.List;

/**
 * RF-08: toda pregunta debe contener un contexto.
 */
public class ContextFilter implements Filter {

    private static final int MIN_CONTEXT_LENGTH = 20;

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        String context = draft.getContext();
        if (context == null || context.isBlank()) {
            throw new ValidationException(List.of("RF-08: la pregunta debe tener un contexto"));
        }
        if (context.trim().length() < MIN_CONTEXT_LENGTH) {
            throw new ValidationException(List.of(
                    "RF-08: el contexto debe tener al menos " + MIN_CONTEXT_LENGTH + " caracteres"));
        }
        return draft;
    }
}
