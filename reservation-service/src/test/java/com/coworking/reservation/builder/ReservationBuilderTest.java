package com.coworking.reservation.builder;

import com.coworking.reservation.client.MemberClient;
import com.coworking.reservation.client.RoomClient;
import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationBuilderTest {
    
    @Mock
    private RoomClient roomClient;
    
    @Mock
    private MemberClient memberClient;
    
    private ReservationBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new ReservationBuilder(roomClient, memberClient);
    }
    
    @Test
    void build_WithValidData_ShouldCreateReservation() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        when(roomClient.checkAvailability(roomId)).thenReturn(Map.of("available", true));
        when(memberClient.isSuspended(memberId)).thenReturn(Map.of("suspended", false));
        
        Reservation reservation = builder
            .withRoomId(roomId)
            .withMemberId(memberId)
            .withTimeSlot(start, end)
            .build();
        
        assertNotNull(reservation);
        assertEquals(roomId, reservation.getRoomId());
        assertEquals(memberId, reservation.getMemberId());
        assertEquals(start, reservation.getStartDateTime());
        assertEquals(end, reservation.getEndDateTime());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }
    
    @Test
    void build_WithUnavailableRoom_ShouldThrowException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        when(roomClient.checkAvailability(roomId)).thenReturn(Map.of("available", false));
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Room is not available for the requested time slot", exception.getMessage());
    }
    
    @Test
    void build_WithSuspendedMember_ShouldThrowException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        when(roomClient.checkAvailability(roomId)).thenReturn(Map.of("available", true));
        when(memberClient.isSuspended(memberId)).thenReturn(Map.of("suspended", true));
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Member is suspended and cannot make reservations", exception.getMessage());
    }
    
    @Test
    void build_WithEndBeforeStart_ShouldThrowException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Start date time must be before end date time", exception.getMessage());
    }
    
    @Test
    void build_WithStartEqualsEnd_ShouldThrowException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start;
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Start date time must be before end date time", exception.getMessage());
    }
    
    @Test
    void build_WithPastStartTime_ShouldThrowException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Cannot create reservation in the past", exception.getMessage());
    }
    
    @Test
    void build_WithNullRoomId_ShouldThrowException() {
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Room ID is required", exception.getMessage());
    }
    
    @Test
    void build_WithNullMemberId_ShouldThrowException() {
        Long roomId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertEquals("Member ID is required", exception.getMessage());
    }
    
    @Test
    void build_WithNullTimeSlot_ShouldThrowException() {
        Long roomId = 1L;
        Long memberId = 1L;
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .build();
        });
        
        assertEquals("Start and end date times are required", exception.getMessage());
    }
    
    @Test
    void build_WithRoomClientException_ShouldThrowBusinessRuleException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        when(roomClient.checkAvailability(roomId)).thenThrow(new RuntimeException("Service unavailable"));
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertTrue(exception.getMessage().contains("Room not found or unavailable"));
    }
    
    @Test
    void build_WithMemberClientException_ShouldThrowBusinessRuleException() {
        Long roomId = 1L;
        Long memberId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        when(roomClient.checkAvailability(roomId)).thenReturn(Map.of("available", true));
        when(memberClient.isSuspended(memberId)).thenThrow(new RuntimeException("Service unavailable"));
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            builder
                .withRoomId(roomId)
                .withMemberId(memberId)
                .withTimeSlot(start, end)
                .build();
        });
        
        assertTrue(exception.getMessage().contains("Member not found or suspended"));
    }
    
    @Test
    void build_MultipleCalls_ShouldResetBuilder() {
        Long roomId1 = 1L;
        Long memberId1 = 1L;
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = start1.plusHours(2);
        
        Long roomId2 = 2L;
        Long memberId2 = 2L;
        LocalDateTime start2 = LocalDateTime.now().plusDays(2);
        LocalDateTime end2 = start2.plusHours(3);
        
        when(roomClient.checkAvailability(anyLong())).thenReturn(Map.of("available", true));
        when(memberClient.isSuspended(anyLong())).thenReturn(Map.of("suspended", false));
        
        Reservation reservation1 = builder
            .withRoomId(roomId1)
            .withMemberId(memberId1)
            .withTimeSlot(start1, end1)
            .build();
        
        Reservation reservation2 = builder
            .withRoomId(roomId2)
            .withMemberId(memberId2)
            .withTimeSlot(start2, end2)
            .build();
        
        assertEquals(roomId1, reservation1.getRoomId());
        assertEquals(roomId2, reservation2.getRoomId());
        assertNotEquals(reservation1.getRoomId(), reservation2.getRoomId());
    }
}
