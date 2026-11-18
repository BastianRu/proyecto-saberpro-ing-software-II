package com.example.gymclass.domain.valueobject;

import java.time.LocalDateTime;

/** Immutable time interval representing one scheduled class session. */
public record ClassSchedule(LocalDateTime startsAt, LocalDateTime endsAt) {
    /** Ensures the interval is complete and has a positive duration. */
    public ClassSchedule {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt))
            throw new IllegalArgumentException("Class end time must be after its start time");
    }
    /** Indicates whether the class has already started at the supplied time. */
    public boolean hasStartedAt(LocalDateTime now) { return !now.isBefore(startsAt); }
}
