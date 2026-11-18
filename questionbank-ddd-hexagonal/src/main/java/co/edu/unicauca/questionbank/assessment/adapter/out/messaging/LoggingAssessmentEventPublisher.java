package co.edu.unicauca.questionbank.assessment.adapter.out.messaging;

import co.edu.unicauca.questionbank.assessment.application.port.out.AssessmentEventPublisherPort;
import co.edu.unicauca.questionbank.assessment.domain.event.AssessmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default secondary adapter: writes the events to the application log. It needs
 * no broker, so the project runs anywhere. Selected with
 * {@code questionbank.assessment.events=log} (the default).
 */
@Component
@ConditionalOnProperty(name = "questionbank.assessment.events", havingValue = "log", matchIfMissing = true)
public class LoggingAssessmentEventPublisher implements AssessmentEventPublisherPort {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingAssessmentEventPublisher.class);

    @Override
    public void publish(AssessmentEvent event) {
        if (event != null) {
            LOG.info("Assessment event: {}", event);
        }
    }
}
