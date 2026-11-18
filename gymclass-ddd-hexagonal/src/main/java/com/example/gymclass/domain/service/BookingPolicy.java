package com.example.gymclass.domain.service;

import com.example.gymclass.domain.model.ClassSession;
import com.example.gymclass.domain.model.Member;
import java.time.LocalDateTime;

/**
 * Domain service that expresses the booking eligibility rule involving two
 * aggregates. It contains domain policy, not persistence or use-case logic.
 */
public class BookingPolicy {
    /** Validates that a member is eligible to book a particular class session. */
    public void ensureEligible(Member member, ClassSession session, LocalDateTime now) {
        if (member == null || session == null || now == null) throw new IllegalArgumentException("Member, session and current time are required");
        if (!member.canAttend(session.type())) throw new IllegalStateException("Member membership is inactive or does not include this class type");
        if (session.schedule().hasStartedAt(now)) throw new IllegalStateException("Class session has already started");
        if (session.availableSpots() <= 0) throw new IllegalStateException("Class session is full");
        if (session.bookings().stream().anyMatch(b -> b.memberId().equals(member.id()) && b.isConfirmed()))
            throw new IllegalStateException("Member already has a confirmed booking for this class");
    }
}
