package com.example.gymclass.adapter.in.web;

import com.example.gymclass.adapter.in.web.dto.ApiRequests.RegisterMemberRequest;
import com.example.gymclass.adapter.in.web.dto.ApiRequests.ScheduleClassRequest;
import com.example.gymclass.application.dto.Responses.*;
import com.example.gymclass.application.dto.Requests;
import com.example.gymclass.application.port.in.GymClassInputPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Primary REST adapter: translates HTTP requests into calls to the inbound application port. */
@RestController
@RequestMapping("/api")
public class GymClassController {
    private final GymClassInputPort inputPort;

    /** Injects the inbound port, not the concrete application service. */
    public GymClassController(GymClassInputPort inputPort) { this.inputPort = inputPort; }

    /** Registers a member. */
    @Transactional
    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse registerMember(@Valid @RequestBody RegisterMemberRequest request) {
        return inputPort.registerMember(new Requests.RegisterMemberRequest(request.fullName(), request.email(), request.plan()));
    }

    /** Retrieves a member. */
    @Transactional(readOnly = true)
    @GetMapping("/members/{id}")
    public MemberResponse getMember(@PathVariable String id) { return inputPort.getMember(id); }

    /** Schedules a group class. */
    @Transactional
    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse scheduleClass(@Valid @RequestBody ScheduleClassRequest request) {
        return inputPort.scheduleClass(new Requests.ScheduleClassRequest(request.title(), request.type(), request.startsAt(), request.endsAt(), request.capacity()));
    }

    /** Lists all class sessions. */
    @Transactional(readOnly = true)
    @GetMapping("/sessions")
    public List<SessionResponse> listSessions() { return inputPort.listSessions(); }

    /** Books a member into a class session. */
    @Transactional
    @PostMapping("/sessions/{sessionId}/bookings/{memberId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookClass(@PathVariable String sessionId, @PathVariable String memberId) {
        return inputPort.bookClass(sessionId, memberId);
    }

    /** Cancels a member's booking. */
    @Transactional
    @DeleteMapping("/sessions/{sessionId}/bookings/{memberId}")
    public SessionResponse cancelBooking(@PathVariable String sessionId, @PathVariable String memberId) {
        return inputPort.cancelBooking(sessionId, memberId);
    }
}
