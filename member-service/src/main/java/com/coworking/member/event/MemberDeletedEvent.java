package com.coworking.member.event;

import java.time.LocalDateTime;

public class MemberDeletedEvent {
    
    private Long memberId;
    private String email;
    private LocalDateTime timestamp;
    
    public MemberDeletedEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public MemberDeletedEvent(Long memberId, String email) {
        this.memberId = memberId;
        this.email = email;
        this.timestamp = LocalDateTime.now();
    }
    
    public Long getMemberId() {
        return memberId;
    }
    
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "MemberDeletedEvent{" +
                "memberId=" + memberId +
                ", email='" + email + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
