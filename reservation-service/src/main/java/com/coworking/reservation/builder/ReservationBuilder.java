package com.coworking.reservation.builder;

import com.coworking.reservation.client.MemberClient;
import com.coworking.reservation.client.RoomClient;
import com.coworking.reservation.dto.MemberDTO;
import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ReservationBuilder {
    
    private final RoomClient roomClient;
    private final MemberClient memberClient;
    
    private Long roomId;
    private Long memberId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    
    public ReservationBuilder(RoomClient roomClient, MemberClient memberClient) {
        this.roomClient = roomClient;
        this.memberClient = memberClient;
    }
    
    public ReservationBuilder withRoomId(Long roomId) {
        this.roomId = roomId;
        return this;
    }
    
    public ReservationBuilder withMemberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }
    
    public ReservationBuilder withTimeSlot(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        return this;
    }
    
    public Reservation build() {
        validateTimeSlot();
        validateRoom();
        validateMember();
        
        Reservation reservation = new Reservation(
            roomId,
            memberId,
            startDateTime,
            endDateTime,
            ReservationStatus.CONFIRMED
        );
        
        reset();
        return reservation;
    }
    
    private void validateTimeSlot() {
        if (roomId == null) {
            throw new BusinessRuleException("Room ID is required");
        }
        if (memberId == null) {
            throw new BusinessRuleException("Member ID is required");
        }
        if (startDateTime == null || endDateTime == null) {
            throw new BusinessRuleException("Start and end date times are required");
        }
        if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
            throw new BusinessRuleException("Start date time must be before end date time");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot create reservation in the past");
        }
    }
    
    private void validateRoom() {
        try {
            Map<String, Boolean> roomAvailability = roomClient.checkAvailabilityForTimeSlot(
                    roomId, 
                    startDateTime.toString(), 
                    endDateTime.toString()
            );
            if (!roomAvailability.get("available")) {
                throw new BusinessRuleException("Room is not available for the requested time slot");
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("Room not found or unavailable: " + e.getMessage());
        }
    }
    
    private void validateMember() {
        try {
            Map<String, Boolean> memberSuspension = memberClient.isSuspended(memberId);
            if (memberSuspension.get("suspended")) {
                throw new BusinessRuleException("Member is suspended and cannot make reservations");
            }
            
            MemberDTO member = memberClient.getMemberById(memberId);
            if (member.getActiveReservationsCount() >= member.getMaxConcurrentBookings()) {
                throw new BusinessRuleException("Member has reached their quota of concurrent reservations");
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("Member not found or suspended: " + e.getMessage());
        }
    }
    
    private void reset() {
        this.roomId = null;
        this.memberId = null;
        this.startDateTime = null;
        this.endDateTime = null;
    }
}
