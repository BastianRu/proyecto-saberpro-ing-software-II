package com.unicauca.lisw2t2g08.banco.api;

import com.unicauca.lisw2t2g08.banco.domain.QuestionState;

/**
 * Peticion para cambiar el estado de una pregunta en el ciclo de vida.
 */
public record ChangeStateRequest(QuestionState state) {
}
