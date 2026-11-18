package com.example.gymclass.application.service;

import com.example.gymclass.application.dto.Requests.*;
import com.example.gymclass.application.dto.Responses.*;
import com.example.gymclass.domain.model.*;
import com.example.gymclass.application.port.out.*;
import com.example.gymclass.application.port.in.GymClassInputPort;
import com.example.gymclass.domain.service.BookingPolicy;
import com.example.gymclass.domain.valueobject.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Application layer: coordinates use cases, output ports and domain behavior. It contains no membership or booking business rules itself.
 */
public class GymClassApplicationService implements GymClassInputPort {
    private final MemberRepositoryPort members;
    private final ClassSessionRepositoryPort sessions;
    private final BookingPolicy bookingPolicy;

    /** Injects domain repository ports and the domain policy. */
    public GymClassApplicationService(MemberRepositoryPort members, ClassSessionRepositoryPort sessions, BookingPolicy bookingPolicy) {
        this.members = members; this.sessions = sessions; this.bookingPolicy = bookingPolicy;
    }
    /** Registers a new member after enforcing unique email address. */
    @Override
    public MemberResponse registerMember(RegisterMemberRequest request) {
        EmailAddress email = new EmailAddress(request.email());
        if (members.existsByEmail(email.value())) throw new IllegalStateException("Email address is already registered");
        Member member = new Member(MemberId.newId(), request.fullName(), email, request.plan());
        return MemberResponse.from(members.save(member));
    }
    /** Creates a class session. */
    @Override
    public SessionResponse scheduleClass(ScheduleClassRequest request) {
        ClassSession session = new ClassSession(SessionId.newId(), request.title(), request.type(), new ClassSchedule(request.startsAt(), request.endsAt()), request.capacity());
        return SessionResponse.from(sessions.save(session));
    }
    /** Executes the Book Class use case by coordinating two aggregates. */
    @Override
    public BookingResponse bookClass(String sessionId, String memberId) {
        Member member = members.findById(MemberId.of(memberId)).orElseThrow(() -> new NoSuchElementException("Member not found"));
        ClassSession session = sessions.findById(SessionId.of(sessionId)).orElseThrow(() -> new NoSuchElementException("Class session not found"));
        LocalDateTime now = LocalDateTime.now();
        bookingPolicy.ensureEligible(member, session, now);
        Booking booking = session.book(member.id(), now);
        sessions.save(session);
        return BookingResponse.from(session.id().value(), booking);
    }
    /** Executes the Cancel Booking use case through the ClassSession aggregate root. */
    @Override
    public SessionResponse cancelBooking(String sessionId, String memberId) {
        ClassSession session = sessions.findById(SessionId.of(sessionId)).orElseThrow(() -> new NoSuchElementException("Class session not found"));
        session.cancelBooking(MemberId.of(memberId), LocalDateTime.now());
        return SessionResponse.from(sessions.save(session));
    }
    /** Lists all scheduled class sessions. */
    @Override
    public List<SessionResponse> listSessions() { return sessions.findAll().stream().map(SessionResponse::from).toList(); }
    /** Returns a member by identity. */
    @Override
    public MemberResponse getMember(String id) {
        return MemberResponse.from(members.findById(MemberId.of(id)).orElseThrow(() -> new NoSuchElementException("Member not found")));
    }
}
