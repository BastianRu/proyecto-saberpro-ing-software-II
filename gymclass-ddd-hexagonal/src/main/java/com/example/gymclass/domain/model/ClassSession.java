package com.example.gymclass.domain.model;

import com.example.gymclass.domain.valueobject.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ClassSession aggregate root. It owns Booking entities and is the only public
 * entry point for booking and cancellation, preserving capacity and uniqueness.
 */
public class ClassSession {
    private final SessionId id;
    private final String title;
    private final ClassType type;
    private final ClassSchedule schedule;
    private final int capacity;
    private final List<Booking> bookings = new ArrayList<>();

    /** Creates a session with a positive capacity and valid schedule. */
    public ClassSession(SessionId id, String title, ClassType type, ClassSchedule schedule, int capacity) {
        if (id == null || type == null || schedule == null) throw new IllegalArgumentException("Session data is required");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Session title is required");
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than zero");
        this.id = id; this.title = title.trim(); this.type = type; this.schedule = schedule; this.capacity = capacity;
    }
    /** Reconstitutes a session and its child bookings from persistence. */
    public static ClassSession reconstitute(SessionId id, String title, ClassType type, ClassSchedule schedule, int capacity, List<Booking> bookings) {
        ClassSession session = new ClassSession(id, title, type, schedule, capacity); session.bookings.addAll(bookings); return session;
    }
    /** Adds a confirmed booking if the session is upcoming, available and unique for the member. */
    public Booking book(MemberId memberId, LocalDateTime now) {
        if (schedule.hasStartedAt(now)) throw new IllegalStateException("Bookings are closed because the class has started");
        if (hasConfirmedBookingFor(memberId)) throw new IllegalStateException("Member already has a confirmed booking for this class");
        if (confirmedBookingsCount() >= capacity) throw new IllegalStateException("Class session is full");
        Booking booking = new Booking(BookingId.newId(), memberId, now); bookings.add(booking); return booking;
    }
    /** Cancels a member's confirmed booking before the class starts. */
    public void cancelBooking(MemberId memberId, LocalDateTime now) {
        if (schedule.hasStartedAt(now)) throw new IllegalStateException("Bookings cannot be cancelled after the class has started");
        Booking booking = bookings.stream().filter(b -> b.memberId().equals(memberId) && b.isConfirmed()).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No confirmed booking found for this member"));
        booking.cancel();
    }
    private boolean hasConfirmedBookingFor(MemberId memberId) { return bookings.stream().anyMatch(b -> b.memberId().equals(memberId) && b.isConfirmed()); }
    /** Counts only confirmed bookings; cancelled reservations release capacity. */
    public int confirmedBookingsCount() { return (int) bookings.stream().filter(Booking::isConfirmed).count(); }
    /** Returns the remaining capacity. */ public int availableSpots() { return capacity - confirmedBookingsCount(); }
    /** Returns a defensive copy of the aggregate's bookings. */ public List<Booking> bookings() { return List.copyOf(bookings); }
    /** Returns the session identity. */ public SessionId id() { return id; }
    /** Returns the session title. */ public String title() { return title; }
    /** Returns the class type. */ public ClassType type() { return type; }
    /** Returns the schedule value object. */ public ClassSchedule schedule() { return schedule; }
    /** Returns the maximum capacity. */ public int capacity() { return capacity; }
}
