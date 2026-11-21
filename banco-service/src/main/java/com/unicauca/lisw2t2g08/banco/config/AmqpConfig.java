package com.unicauca.lisw2t2g08.banco.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.lisw2t2g08.banco.events.RabbitEventPublisher;

/** Infraestructura de mensajería: el exchange donde el Banco publica y el formato JSON de los eventos. */
@Configuration
public class AmqpConfig {

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(RabbitEventPublisher.EXCHANGE, true, false);
    }

    // Se reutiliza el ObjectMapper de Spring para que las fechas viajen en ISO-8601
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
