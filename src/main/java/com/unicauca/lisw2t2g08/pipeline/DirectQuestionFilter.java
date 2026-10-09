package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.ArrayList;
import java.util.List;

/**
 * RF-09: debe existir una unica pregunta directa, con formato de pregunta.
 */
public class DirectQuestionFilter implements Filter {

    private static final int MIN_QUESTION_LENGTH = 10;

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        String question = draft.getDirectQuestion();
        if (question == null || question.isBlank()) {
            throw new ValidationException(List.of("RF-09: debe existir una unica pregunta directa"));
        }

        String trimmed = question.trim();
        List<String> errors = new ArrayList<>();
        if (trimmed.length() < MIN_QUESTION_LENGTH) {
            errors.add("RF-09: la pregunta directa es demasiado corta");
        }
        if (!trimmed.endsWith("?")) {
            errors.add("RF-09: la pregunta directa debe terminar en '?'");
        }
        if (trimmed.chars().filter(c -> c == '?').count() > 1) {
            errors.add("RF-09: debe existir una unica pregunta directa, no varias");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return draft;
    }
}
