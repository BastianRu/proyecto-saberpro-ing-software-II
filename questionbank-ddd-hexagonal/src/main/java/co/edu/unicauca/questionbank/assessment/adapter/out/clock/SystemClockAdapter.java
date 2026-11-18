package co.edu.unicauca.questionbank.assessment.adapter.out.clock;

import co.edu.unicauca.questionbank.assessment.application.port.out.ClockPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Secondary (driven) adapter that supplies the system time to the core. */
@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
