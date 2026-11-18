package com.example.gymclass.adapter.in.web.dto;

import com.example.gymclass.domain.valueobject.ClassType;
import com.example.gymclass.domain.valueobject.MembershipPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/** HTTP-specific request models. Validation annotations stay in the web adapter, outside the application core. */
public final class ApiRequests {
    private ApiRequests() { }

    /** JSON payload used to register a member. */
    public record RegisterMemberRequest(@NotBlank String fullName, @NotBlank @Email String email,
                                        @NotNull MembershipPlan plan) { }

    /** JSON payload used to schedule a class session. */
    public record ScheduleClassRequest(@NotBlank String title, @NotNull ClassType type,
                                       @NotNull LocalDateTime startsAt, @NotNull LocalDateTime endsAt,
                                       @Positive int capacity) { }
}
