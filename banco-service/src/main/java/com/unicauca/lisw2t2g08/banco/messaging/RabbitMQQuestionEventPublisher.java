package com.unicauca.lisw2t2g08.banco.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Publica eventos en RabbitMQ a traves del exchange configurado.
 *
 * Si el broker RabbitMQ no estuviera disponible temporalmente en el entorno local,
 * registra una advertencia para no bloquear las transacciones principales del servicio.
 */
public class RabbitMQQuestionEventPublisher implements IQuestionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQQuestionEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    public RabbitMQQuestionEventPublisher(RabbitTemplate rabbitTemplate, String exchangeName) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    @Override
    public void publish(QuestionEvent event) {
        if (event == null) {
            return;
        }

        String routingKey = "question." + event.eventType().toLowerCase();
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
            log.info("Evento publicado en RabbitMQ [{}]: {}", routingKey, event);
        } catch (Exception e) {
            log.warn("No fue posible publicar el evento en RabbitMQ (exchange: {}): {}. Detalle: {}",
                    exchangeName, event, e.getMessage());
        }
    }
}
