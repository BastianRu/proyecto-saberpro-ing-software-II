package com.example.gymclass.domain.valueobject;

import java.util.regex.Pattern;

/** Immutable, validated email address value object. */
public record EmailAddress(String value) {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    /** Validates and normalizes an email address. */
    public EmailAddress {
        if (value == null || !EMAIL.matcher(value.trim()).matches()) throw new IllegalArgumentException("A valid email address is required");
        value = value.trim().toLowerCase();
    }
    @Override public String toString() { return value; }
}
