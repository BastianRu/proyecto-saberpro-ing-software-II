package com.unicauca.lisw2t2g08.banco.events;

/**
 * Puerto de salida para publicar eventos de dominio. El servicio depende de esta
 * interfaz y no de RabbitMQ (DIP): en pruebas se reemplaza por un publicador en memoria.
 */
public interface IEventPublisher {

    void publish(QuestionCreatedEvent event);

    void publish(QuestionStateChangedEvent event);
}
