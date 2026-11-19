package com.unicauca.lisw2t2g08.auditoria.events;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.unicauca.lisw2t2g08.auditoria.config.AmqpConfig;
import com.unicauca.lisw2t2g08.auditoria.service.AuditService;

/** Consumidor de la cola de auditoría: recibe los eventos del Banco y los delega al servicio. */
@Component
public class QuestionEventListener {

    private final AuditService service;

    public QuestionEventListener(AuditService service) {
        this.service = service;
    }

    @RabbitListener(queues = AmqpConfig.QUEUE)
    public void onEvent(@Payload QuestionEventMessage message,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        service.record(routingKey, message);
    }
}
