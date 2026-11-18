package com.example.gymclass.adapter.out.persistence;

import com.example.gymclass.domain.model.*;
import com.example.gymclass.domain.valueobject.*;
import jakarta.persistence.*;
import java.util.*;

/** JPA persistence representation of the ClassSession aggregate. */
@Entity @Table(name="class_sessions")
public class ClassSessionJpaEntity {
    @Id UUID id; @Column(nullable=false) String title;
    @Enumerated(EnumType.STRING) @Column(nullable=false) ClassType type;
    @Column(nullable=false) java.time.LocalDateTime startsAt; @Column(nullable=false) java.time.LocalDateTime endsAt;
    @Column(nullable=false) int capacity;
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    @JoinColumn(name="session_id") List<BookingJpaEntity> bookings=new ArrayList<>();
    protected ClassSessionJpaEntity() {}
    /** Maps the complete aggregate, including its owned bookings, to persistence. */
    static ClassSessionJpaEntity fromDomain(ClassSession s) {
        ClassSessionJpaEntity e=new ClassSessionJpaEntity(); e.id=s.id().value(); e.title=s.title(); e.type=s.type();
        e.startsAt=s.schedule().startsAt(); e.endsAt=s.schedule().endsAt(); e.capacity=s.capacity();
        e.bookings=s.bookings().stream().map(BookingJpaEntity::fromDomain).toList().stream().collect(java.util.stream.Collectors.toCollection(ArrayList::new)); return e;
    }
    /** Rebuilds the aggregate and all its child entities from persistence. */
    ClassSession toDomain() {
        return ClassSession.reconstitute(new SessionId(id), title, type, new ClassSchedule(startsAt, endsAt), capacity, bookings.stream().map(BookingJpaEntity::toDomain).toList());
    }
}
