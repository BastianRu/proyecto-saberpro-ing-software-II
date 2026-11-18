package co.edu.unicauca.questionbank.assessment.adapter.out.messaging;

import co.edu.unicauca.questionbank.assessment.application.port.out.AssessmentEventPublisherPort;
import co.edu.unicauca.questionbank.assessment.domain.event.AssessmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Locale;

/**
 * Secondary adapter that publishes the events to a RabbitMQ topic exchange with
 * the routing key {@code assessment.<event type>}. Reused from the
 * {@code RabbitMQQuestionEventPublisher} of banco-service: if the broker is not
 * available it logs a warning instead of breaking the use case.
 */
public class RabbitMqAssessmentEventPublisher implements AssessmentEventPublisherPort {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMqAssessmentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    /**
     * @param rabbitTemplate Spring AMQP template (with a JSON converter)
     * @param exchangeName   topic exchange where events are published
     */
    public RabbitMqAssessmentEventPublisher(RabbitTemplate rabbitTemplate, String exchangeName) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    @Override
    public void publish(AssessmentEvent event) {
        if (event == null) {
            return;
        }
        String routingKey = "assessment." + event.eventType().toLowerCase(Locale.ROOT);
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
            LOG.info("Event published to RabbitMQ [{}]: {}", routingKey, event);
        } catch (Exception e) {
            LOG.warn("Could not publish the event to RabbitMQ (exchange {}): {}. Cause: {}",
                    exchangeName, event, e.getMessage());
        }
    }
}
