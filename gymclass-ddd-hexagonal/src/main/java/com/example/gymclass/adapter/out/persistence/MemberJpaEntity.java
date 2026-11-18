package com.example.gymclass.adapter.out.persistence;

import com.example.gymclass.domain.model.Member;
import com.example.gymclass.domain.valueobject.*;
import jakarta.persistence.*;
import java.util.UUID;

/** JPA persistence representation of the Member aggregate (not a domain entity). */
@Entity @Table(name="members", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class MemberJpaEntity {
    @Id UUID id; @Column(nullable=false) String fullName; @Column(nullable=false, unique=true) String email;
    @Enumerated(EnumType.STRING) @Column(nullable=false) MembershipPlan plan;
    @Enumerated(EnumType.STRING) @Column(nullable=false) MembershipStatus status;
    protected MemberJpaEntity() {}
    /** Maps a domain member to its persistence representation. */
    static MemberJpaEntity fromDomain(Member m) { MemberJpaEntity e=new MemberJpaEntity(); e.id=m.id().value(); e.fullName=m.fullName(); e.email=m.email().value(); e.plan=m.plan(); e.status=m.status(); return e; }
    /** Rebuilds the domain aggregate from persisted state. */
    Member toDomain() { return Member.reconstitute(new MemberId(id), fullName, new EmailAddress(email), plan, status); }
}
