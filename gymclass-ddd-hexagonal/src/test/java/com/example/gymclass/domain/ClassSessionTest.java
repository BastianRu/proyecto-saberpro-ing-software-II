package com.example.gymclass.domain;

import com.example.gymclass.domain.model.*;
import com.example.gymclass.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the ClassSession aggregate invariants. */
class ClassSessionTest {
    @Test void shouldRejectBookingWhenCapacityIsReached() {
        LocalDateTime start=LocalDateTime.now().plusDays(1);
        ClassSession session=new ClassSession(SessionId.newId(),"Morning Yoga",ClassType.YOGA,new ClassSchedule(start,start.plusHours(1)),1);
        session.book(MemberId.newId(),LocalDateTime.now());
        assertThrows(IllegalStateException.class,()->session.book(MemberId.newId(),LocalDateTime.now()));
    }
    @Test void cancelledBookingShouldReleaseCapacity() {
        LocalDateTime start=LocalDateTime.now().plusDays(1);
        ClassSession session=new ClassSession(SessionId.newId(),"Morning Yoga",ClassType.YOGA,new ClassSchedule(start,start.plusHours(1)),1);
        MemberId member=MemberId.newId(); session.book(member,LocalDateTime.now()); session.cancelBooking(member,LocalDateTime.now());
        assertEquals(1,session.availableSpots());
    }
}
