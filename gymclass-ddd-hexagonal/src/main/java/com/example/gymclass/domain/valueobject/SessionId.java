package com.example.gymclass.domain.valueobject;

import java.util.UUID;

/** Immutable identity value object for a class session. */
public record SessionId(UUID value) {
    /** Validates the identity. */
    public SessionId { if (value == null) throw new IllegalArgumentException("Session id is required"); }
    /** Generates a new identity. */
    public static SessionId newId() { return new SessionId(UUID.randomUUID()); }
    /** Parses an identity. */
    public static SessionId of(String value) { return new SessionId(UUID.fromString(value)); }
    @Override public String toString() { return value.toString(); }
}
