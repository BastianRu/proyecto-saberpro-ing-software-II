package com.example.gymclass.application.dto;

import com.example.gymclass.domain.valueobject.*;
import java.time.LocalDateTime;

/** Framework-independent command records consumed by application use cases. */
public final class Requests {
    private Requests() {}
    /** Input required to register a gym member. */
    public record RegisterMemberRequest(String fullName, String email, MembershipPlan plan) {}
    /** Input required to schedule a group class. */
    public record ScheduleClassRequest(String title, ClassType type, LocalDateTime startsAt, LocalDateTime endsAt, int capacity) {}
}
