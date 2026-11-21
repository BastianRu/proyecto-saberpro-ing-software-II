package com.unicauca.lisw2t2g08.revision.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Topología de mensajería del consumidor. Revisión declara su propia cola durable y la enlaza al
 * exchange del Banco solo con "question.state-changed": no le interesa la creación de preguntas.
 */
@Configuration
public class AmqpConfig {

    public static final String EXCHANGE = "saberpro.events";
    public static final String QUEUE = "revision.preguntas";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue reviewQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding reviewBinding(Queue reviewQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(reviewQueue).to(eventsExchange).with("question.state-changed");
    }

    // INFERRED: el tipo a deserializar sale del parámetro del @RabbitListener, no de la cabecera
    // __TypeId__ (que trae el nombre de la clase del Banco, inexistente aquí: no se comparten clases).
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(TypePrecedence.INFERRED);
        return converter;
    }
}
