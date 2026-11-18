package com.example.gymclass.domain.valueobject;

import java.util.UUID;

/** Immutable identity value object for a booking. */
public record BookingId(UUID value) {
    /** Validates the identity. */
    public BookingId { if (value == null) throw new IllegalArgumentException("Booking id is required"); }
    /** Generates a new identity. */
    public static BookingId newId() { return new BookingId(UUID.randomUUID()); }
    @Override public String toString() { return value.toString(); }
}
