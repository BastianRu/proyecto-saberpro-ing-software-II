package com.example.gymclass.domain.valueobject;

/** Plans define which class types a member is allowed to attend. */
public enum MembershipPlan {
    BASIC, STANDARD, PREMIUM;

    /** Determines whether this plan grants access to the requested class type. */
    public boolean allows(ClassType type) {
        return switch (this) {
            case BASIC -> type == ClassType.YOGA;
            case STANDARD -> type == ClassType.YOGA || type == ClassType.PILATES;
            case PREMIUM -> true;
        };
    }
}
