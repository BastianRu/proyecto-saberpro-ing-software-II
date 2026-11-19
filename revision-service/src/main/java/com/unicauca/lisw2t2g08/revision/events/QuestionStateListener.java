package com.unicauca.lisw2t2g08.revision.events;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.unicauca.lisw2t2g08.revision.config.AmqpConfig;
import com.unicauca.lisw2t2g08.revision.service.ReviewService;

/** Consumidor de la cola de revisión: recibe QuestionStateChanged y lo delega al servicio. */
@Component
public class QuestionStateListener {

    private final ReviewService service;

    public QuestionStateListener(ReviewService service) {
        this.service = service;
    }

    @RabbitListener(queues = AmqpConfig.QUEUE)
    public void onStateChanged(QuestionStateMessage message) {
        service.onQuestionStateChanged(message);
    }
}
