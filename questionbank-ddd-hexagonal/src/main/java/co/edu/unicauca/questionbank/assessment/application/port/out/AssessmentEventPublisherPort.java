package co.edu.unicauca.questionbank.assessment.application.port.out;

import co.edu.unicauca.questionbank.assessment.domain.event.AssessmentEvent;

/**
 * Secondary (outbound) port to notify other systems about attempts (for example,
 * a statistics or notification service). The core does not know whether the
 * event goes to a log, to RabbitMQ or elsewhere. Adapted from the
 * {@code IQuestionEventPublisher} of banco-service (final project).
 */
public interface AssessmentEventPublisherPort {

    /**
     * Publishes an event. Implementations must not break the use case if the
     * destination is unavailable.
     *
     * @param event event to publish
     */
    void publish(AssessmentEvent event);
}
