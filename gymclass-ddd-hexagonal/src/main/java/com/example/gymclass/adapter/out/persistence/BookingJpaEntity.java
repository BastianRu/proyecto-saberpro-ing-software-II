package com.example.gymclass.adapter.out.persistence;

import com.example.gymclass.domain.model.Booking;
import com.example.gymclass.domain.valueobject.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** JPA persistence representation of a Booking child entity. */
@Entity @Table(name="bookings")
public class BookingJpaEntity {
    @Id UUID id; @Column(name="member_id", nullable=false) UUID memberId; @Column(nullable=false) LocalDateTime bookedAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false) Booking.Status status;
    protected BookingJpaEntity() {}
    /** Maps a domain booking to JPA state. */
    static BookingJpaEntity fromDomain(Booking b) { BookingJpaEntity e=new BookingJpaEntity(); e.id=b.id().value(); e.memberId=b.memberId().value(); e.bookedAt=b.bookedAt(); e.status=b.status(); return e; }
    /** Maps persisted state back to a domain booking. */
    Booking toDomain() { return Booking.reconstitute(new BookingId(id), new MemberId(memberId), bookedAt, status); }
}
