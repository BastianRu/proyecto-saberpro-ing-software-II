package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.ArrayList;
import java.util.List;

/**
 * RF-05: la pregunta debe traer la justificacion de la respuesta, la
 * bibliografia, la competencia, el tema, el subtema y el nivel de dificultad.
 * El contexto, la pregunta directa y las opciones los validan sus propios
 * filtros.
 */
public class RequiredFieldsFilter implements Filter {

    @Override
    public QuestionDraft process(QuestionDraft draft) {
        List<String> errors = new ArrayList<>();
        require(errors, draft.getName(), "el nombre de la pregunta");
        require(errors, draft.getJustification(), "la justificacion de la respuesta");
        require(errors, draft.getBibliography(), "la bibliografia");
        require(errors, draft.getCompetence(), "la competencia");
        require(errors, draft.getTopic(), "el tema");
        require(errors, draft.getSubtopic(), "el subtema");
        if (draft.getDifficulty() == null) {
            errors.add("RF-05: debe indicarse el nivel de dificultad");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return draft;
    }

    private static void require(List<String> errors, String value, String field) {
        if (value == null || value.isBlank()) {
            errors.add("RF-05: falta " + field);
        }
    }
}
