package com.unicauca.lisw2t2g08.banco.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Adaptador de IEventPublisher hacia RabbitMQ. Publica en un exchange de tipo topic;
 * cada consumidor declara su propia cola y se enlaza con la clave de enrutamiento que le interesa.
 */
public class RabbitEventPublisher implements IEventPublisher {

    public static final String EXCHANGE = "saberpro.events";
    public static final String QUESTION_CREATED_KEY = "question.created";
    public static final String QUESTION_STATE_CHANGED_KEY = "question.state-changed";

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(QuestionCreatedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, QUESTION_CREATED_KEY, event);
    }

    @Override
    public void publish(QuestionStateChangedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, QUESTION_STATE_CHANGED_KEY, event);
    }
}
