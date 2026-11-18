package com.example.gymclass.application.port.in;

import com.example.gymclass.application.dto.Requests.RegisterMemberRequest;
import com.example.gymclass.application.dto.Requests.ScheduleClassRequest;
import com.example.gymclass.application.dto.Responses.BookingResponse;
import com.example.gymclass.application.dto.Responses.MemberResponse;
import com.example.gymclass.application.dto.Responses.SessionResponse;
import java.util.List;

/**
 * Primary (inbound) port exposing the application use cases to driving adapters.
 * This contract is independent of HTTP, Spring MVC and persistence technology.
 */
public interface GymClassInputPort {
    /** Registers a gym member. */
    MemberResponse registerMember(RegisterMemberRequest request);
    /** Retrieves a member by its external identifier. */
    MemberResponse getMember(String id);
    /** Schedules a new group class. */
    SessionResponse scheduleClass(ScheduleClassRequest request);
    /** Lists all scheduled class sessions. */
    List<SessionResponse> listSessions();
    /** Books a member into a class session. */
    BookingResponse bookClass(String sessionId, String memberId);
    /** Cancels a member's booking. */
    SessionResponse cancelBooking(String sessionId, String memberId);
}
