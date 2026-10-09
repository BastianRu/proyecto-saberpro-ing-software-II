package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

/**
 * Contrato del patron Tuberias y Filtros. Cada filtro valida un aspecto de la
 * pregunta y, si es correcto, devuelve el borrador para que el siguiente
 * filtro continue procesandolo.
 */
public interface Filter {

    /**
     * @param draft borrador recibido del filtro anterior (o el original).
     * @return el borrador, listo para el siguiente filtro.
     * @throws ValidationException si el borrador incumple las reglas de este filtro.
     */
    QuestionDraft process(QuestionDraft draft);
}
