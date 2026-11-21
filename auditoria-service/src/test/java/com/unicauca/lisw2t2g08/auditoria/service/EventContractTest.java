package com.unicauca.lisw2t2g08.auditoria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unicauca.lisw2t2g08.auditoria.config.AmqpConfig;
import com.unicauca.lisw2t2g08.auditoria.events.QuestionEventMessage;

/**
 * Contrato con el Banco: el JSON que publica el Banco (con la cabecera __TypeId__ de SU clase, que aquí
 * no existe) debe poder leerse en la clase propia de este servicio.
 */
class EventContractTest {

    @Test
    void deserializaElEventoDelBancoIgnorandoLaCabeceraTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Jackson2JsonMessageConverter converter =
                (Jackson2JsonMessageConverter) new AmqpConfig().messageConverter(mapper);

        String json = "{\"eventId\":\"e1\",\"questionId\":\"P-001\",\"previousState\":\"BORRADOR\","
                + "\"newState\":\"PENDIENTE_REVISION\",\"username\":\"ana\",\"occurredAt\":\"2026-11-19T15:00:00Z\"}";
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setHeader("__TypeId__", "com.unicauca.lisw2t2g08.banco.events.QuestionStateChangedEvent");
        props.setInferredArgumentType(QuestionEventMessage.class); // lo que hace @RabbitListener según el parámetro

        Object result = converter.fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), props));

        QuestionEventMessage message = (QuestionEventMessage) result;
        assertEquals("PENDIENTE_REVISION", message.newState());
        assertEquals(Instant.parse("2026-11-19T15:00:00Z"), message.occurredAt());
    }
}
