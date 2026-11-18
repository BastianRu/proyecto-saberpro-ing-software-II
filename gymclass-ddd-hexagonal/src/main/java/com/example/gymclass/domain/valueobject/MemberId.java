package com.example.gymclass.domain.valueobject;

import java.util.UUID;

/** Immutable identity value object for a gym member. */
public record MemberId(UUID value) {
    /** Creates a new member identity. */
    public MemberId { if (value == null) throw new IllegalArgumentException("Member id is required"); }
    /** Generates a new identity. */
    public static MemberId newId() { return new MemberId(UUID.randomUUID()); }
    /** Parses an identity from its external representation. */
    public static MemberId of(String value) { return new MemberId(UUID.fromString(value)); }
    @Override public String toString() { return value.toString(); }
}
