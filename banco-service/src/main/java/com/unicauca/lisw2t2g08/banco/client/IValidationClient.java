package com.unicauca.lisw2t2g08.banco.client;

import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;

/**
 * Cliente para el microservicio de Validacion Estructural (HU03).
 */
public interface IValidationClient {

    /**
     * Envia el borrador al microservicio de validacion.
     *
     * @param draft borrador de la pregunta a validar.
     * @throws ValidationException si algun filtro del pipeline rechaza el borrador.
     */
    void validate(QuestionDraft draft);
}
