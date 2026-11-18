package com.example.gymclass.domain.model;

import com.example.gymclass.domain.valueobject.*;

/**
 * Member aggregate root. It protects membership identity, contact information,
 * plan and lifecycle rules; callers cannot mutate its state through setters.
 */
public class Member {
    private final MemberId id;
    private String fullName;
    private final EmailAddress email;
    private MembershipPlan plan;
    private MembershipStatus status;

    /** Creates a member with an active membership. */
    public Member(MemberId id, String fullName, EmailAddress email, MembershipPlan plan) {
        if (id == null || email == null || plan == null) throw new IllegalArgumentException("Member data is required");
        this.id = id; rename(fullName); this.email = email; this.plan = plan; this.status = MembershipStatus.ACTIVE;
    }
    /** Reconstitutes a member from persisted domain state. */
    public static Member reconstitute(MemberId id, String fullName, EmailAddress email, MembershipPlan plan, MembershipStatus status) {
        Member member = new Member(id, fullName, email, plan); member.status = status; return member;
    }
    /** Changes the member's display name after validating it. */
    public void rename(String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("Member name is required");
        this.fullName = newName.trim();
    }
    /** Suspends an active membership. */
    public void suspendMembership() { this.status = MembershipStatus.SUSPENDED; }
    /** Reactivates a suspended membership. */
    public void activateMembership() { this.status = MembershipStatus.ACTIVE; }
    /** Changes the commercial plan. */
    public void changePlan(MembershipPlan newPlan) {
        if (newPlan == null) throw new IllegalArgumentException("Membership plan is required"); this.plan = newPlan;
    }
    /** Checks whether the member is active and the plan allows the class type. */
    public boolean canAttend(ClassType type) { return status == MembershipStatus.ACTIVE && plan.allows(type); }
    /** Returns the member identity. */ public MemberId id() { return id; }
    /** Returns the member name. */ public String fullName() { return fullName; }
    /** Returns the email value object. */ public EmailAddress email() { return email; }
    /** Returns the membership plan. */ public MembershipPlan plan() { return plan; }
    /** Returns the membership status. */ public MembershipStatus status() { return status; }
}
