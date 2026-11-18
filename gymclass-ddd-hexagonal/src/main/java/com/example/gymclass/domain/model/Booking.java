package com.example.gymclass.domain.model;

import com.example.gymclass.domain.valueobject.BookingId;
import com.example.gymclass.domain.valueobject.MemberId;
import java.time.LocalDateTime;

/** Booking entity owned by the ClassSession aggregate. */
public class Booking {
    public enum Status { CONFIRMED, CANCELLED }
    private final BookingId id;
    private final MemberId memberId;
    private final LocalDateTime bookedAt;
    private Status status;

    /** Creates a confirmed booking. */
    public Booking(BookingId id, MemberId memberId, LocalDateTime bookedAt) {
        if (id == null || memberId == null || bookedAt == null) throw new IllegalArgumentException("Booking data is required");
        this.id = id; this.memberId = memberId; this.bookedAt = bookedAt; this.status = Status.CONFIRMED;
    }
    /** Reconstitutes a booking from persisted state. */
    public static Booking reconstitute(BookingId id, MemberId memberId, LocalDateTime bookedAt, Status status) {
        Booking booking = new Booking(id, memberId, bookedAt); booking.status = status; return booking;
    }
    /** Cancels this booking; repeated cancellation is rejected. */
    public void cancel() { if (status == Status.CANCELLED) throw new IllegalStateException("Booking is already cancelled"); status = Status.CANCELLED; }
    /** Indicates whether this booking is currently confirmed. */
    public boolean isConfirmed() { return status == Status.CONFIRMED; }
    /** Returns the booking identity. */ public BookingId id() { return id; }
    /** Returns the booked member identity. */ public MemberId memberId() { return memberId; }
    /** Returns the booking timestamp. */ public LocalDateTime bookedAt() { return bookedAt; }
    /** Returns the booking status. */ public Status status() { return status; }
}
