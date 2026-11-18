package com.example.gymclass.config;

import com.example.gymclass.domain.service.BookingPolicy;
import com.example.gymclass.application.service.GymClassApplicationService;
import com.example.gymclass.application.port.in.GymClassInputPort;
import com.example.gymclass.application.port.out.MemberRepositoryPort;
import com.example.gymclass.application.port.out.ClassSessionRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers domain services that are plain Java objects rather than Spring components. */
@Configuration
public class HexagonalConfiguration {
    /** Creates the domain booking policy. */
    @Bean public BookingPolicy bookingPolicy() { return new BookingPolicy(); }

    /** Wires the application core to outbound ports without making the core a Spring component. */
    @Bean public GymClassInputPort gymClassInputPort(MemberRepositoryPort members,
                                                       ClassSessionRepositoryPort sessions,
                                                       BookingPolicy bookingPolicy) {
        return new GymClassApplicationService(members, sessions, bookingPolicy);
    }
}
