package com.coworking.member.event;

import java.time.LocalDateTime;

public class ReservationCreatedEvent {
    
    private Long reservationId;
    private Long memberId;
    private Long roomId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime timestamp;
    
    public ReservationCreatedEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ReservationCreatedEvent(Long reservationId, Long memberId, Long roomId, 
                                   LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.roomId = roomId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.timestamp = LocalDateTime.now();
    }
    
    public Long getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    
    public Long getMemberId() {
        return memberId;
    }
    
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
    
    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }
    
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
    
    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ReservationCreatedEvent{" +
                "reservationId=" + reservationId +
                ", memberId=" + memberId +
                ", roomId=" + roomId +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", timestamp=" + timestamp +
                '}';
    }
}
