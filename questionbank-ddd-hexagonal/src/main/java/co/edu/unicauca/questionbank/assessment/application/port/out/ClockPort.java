package co.edu.unicauca.questionbank.assessment.application.port.out;

import java.time.LocalDateTime;

/**
 * Secondary (outbound) port that supplies the current time. Deadlines depend on
 * time, so making it a port lets tests control it without touching the core.
 */
public interface ClockPort {

    /** @return the current moment */
    LocalDateTime now();
}
