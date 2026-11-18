package com.example.gymclass.application.dto;

import com.example.gymclass.domain.model.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Application response models returned through the inbound port. */
public final class Responses {
    private Responses() {}
    /** Public representation of a member. */
    public record MemberResponse(UUID id, String fullName, String email, String plan, String status) {
        /** Maps a domain member to its API representation. */
        public static MemberResponse from(Member m) { return new MemberResponse(m.id().value(), m.fullName(), m.email().value(), m.plan().name(), m.status().name()); }
    }
    /** Public representation of a class session and its capacity. */
    public record SessionResponse(UUID id, String title, String type, LocalDateTime startsAt, LocalDateTime endsAt, int capacity, int confirmedBookings, int availableSpots) {
        /** Maps a domain session to its API representation. */
        public static SessionResponse from(ClassSession s) { return new SessionResponse(s.id().value(), s.title(), s.type().name(), s.schedule().startsAt(), s.schedule().endsAt(), s.capacity(), s.confirmedBookingsCount(), s.availableSpots()); }
    }
    /** Public representation of a booking result. */
    public record BookingResponse(UUID bookingId, UUID sessionId, UUID memberId, String status, LocalDateTime bookedAt) {
        /** Maps a booking entity to its API representation. */
        public static BookingResponse from(UUID sessionId, Booking b) { return new BookingResponse(b.id().value(), sessionId, b.memberId().value(), b.status().name(), b.bookedAt()); }
    }
}
