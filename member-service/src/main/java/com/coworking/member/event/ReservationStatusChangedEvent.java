package com.coworking.member.event;

import java.time.LocalDateTime;

public class ReservationStatusChangedEvent {
    
    private Long reservationId;
    private Long memberId;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime timestamp;
    
    public ReservationStatusChangedEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ReservationStatusChangedEvent(Long reservationId, Long memberId, 
                                         String previousStatus, String newStatus) {
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
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
    
    public String getPreviousStatus() {
        return previousStatus;
    }
    
    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ReservationStatusChangedEvent{" +
                "reservationId=" + reservationId +
                ", memberId=" + memberId +
                ", previousStatus='" + previousStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
