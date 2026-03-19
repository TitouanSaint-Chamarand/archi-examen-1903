package com.coworking.reservation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long roomId;
    
    @Column(nullable = false)
    private Long memberId;
    
    @Column(nullable = false)
    private LocalDateTime startDateTime;
    
    @Column(nullable = false)
    private LocalDateTime endDateTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    public Reservation() {
    }
    
    public Reservation(Long roomId, Long memberId, LocalDateTime startDateTime, 
                       LocalDateTime endDateTime, ReservationStatus status) {
        this.roomId = roomId;
        this.memberId = memberId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public Long getMemberId() {
        return memberId;
    }
    
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
