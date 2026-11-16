package com.unicauca.lisw2t2g08.banco.messaging;

/**
 * Publicador de eventos asincronos del banco de preguntas.
 */
public interface IQuestionEventPublisher {

    void publish(QuestionEvent event);
}
