package co.edu.unicauca.questionbank.assessment.config;

import co.edu.unicauca.questionbank.assessment.adapter.out.messaging.RabbitMqAssessmentEventPublisher;
import co.edu.unicauca.questionbank.assessment.application.port.out.AssessmentEventPublisherPort;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the RabbitMQ event adapter. Active only with
 * {@code questionbank.assessment.events=rabbitmq}; switching adapters is a
 * configuration change, the core is not modified.
 */
@Configuration
@ConditionalOnProperty(name = "questionbank.assessment.events", havingValue = "rabbitmq")
public class RabbitMqMessagingConfiguration {

    /**
     * @param name exchange name
     * @return the durable topic exchange for assessment events
     */
    @Bean
    public TopicExchange assessmentExchange(@Value("${questionbank.assessment.rabbitmq.exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    /**
     * @param connectionFactory RabbitMQ connection factory
     * @return a template that sends JSON messages
     */
    @Bean
    public RabbitTemplate assessmentRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    /**
     * @param assessmentRabbitTemplate JSON template
     * @param exchange                 destination exchange
     * @return the RabbitMQ implementation of the event port
     */
    @Bean
    public AssessmentEventPublisherPort rabbitMqAssessmentEventPublisher(RabbitTemplate assessmentRabbitTemplate,
                                                                         TopicExchange exchange) {
        return new RabbitMqAssessmentEventPublisher(assessmentRabbitTemplate, exchange.getName());
    }
}
