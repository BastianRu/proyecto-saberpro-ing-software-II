package co.edu.unicauca.questionbank.assessment.config;

import co.edu.unicauca.questionbank.assessment.application.port.in.MockExamInputPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.AssessmentEventPublisherPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ClockPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ExamAttemptRepositoryPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.ExamSheetProviderPort;
import co.edu.unicauca.questionbank.assessment.application.port.out.StudentDirectoryPort;
import co.edu.unicauca.questionbank.assessment.application.service.MockExamApplicationService;
import co.edu.unicauca.questionbank.assessment.domain.service.AttemptPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root of the assessment hexagon. The domain service and the
 * application service are plain Java objects; here Spring connects them to the
 * concrete adapters that implement the outbound ports.
 */
@Configuration
public class AssessmentConfiguration {

    /** @return the domain policy to start attempts */
    @Bean
    public AttemptPolicy attemptPolicy() {
        return new AttemptPolicy();
    }

    /**
     * Wires the core to its outbound ports.
     *
     * @param attempts attempt repository adapter
     * @param sheets   exam sheet adapter (question bank)
     * @param students student directory adapter (question bank)
     * @param clock    clock adapter
     * @param policy   domain policy
     * @param events   event publisher adapter (log or RabbitMQ)
     * @return the inbound port
     */
    @Bean
    public MockExamInputPort mockExamInputPort(ExamAttemptRepositoryPort attempts, ExamSheetProviderPort sheets,
                                               StudentDirectoryPort students, ClockPort clock,
                                               AttemptPolicy policy, AssessmentEventPublisherPort events) {
        return new MockExamApplicationService(attempts, sheets, students, clock, policy, events);
    }
}
