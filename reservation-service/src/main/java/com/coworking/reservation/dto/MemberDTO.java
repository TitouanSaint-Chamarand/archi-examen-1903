package com.coworking.reservation.dto;

public class MemberDTO {
    
    private Long id;
    private String fullName;
    private boolean suspended;
    private Integer maxConcurrentBookings;
    private int activeReservationsCount;
    
    public MemberDTO() {
    }
    
    public MemberDTO(Long id, String fullName, boolean suspended, Integer maxConcurrentBookings, int activeReservationsCount) {
        this.id = id;
        this.fullName = fullName;
        this.suspended = suspended;
        this.maxConcurrentBookings = maxConcurrentBookings;
        this.activeReservationsCount = activeReservationsCount;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public boolean isSuspended() {
        return suspended;
    }
    
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }
    
    public Integer getMaxConcurrentBookings() {
        return maxConcurrentBookings;
    }
    
    public void setMaxConcurrentBookings(Integer maxConcurrentBookings) {
        this.maxConcurrentBookings = maxConcurrentBookings;
    }
    
    public int getActiveReservationsCount() {
        return activeReservationsCount;
    }
    
    public void setActiveReservationsCount(int activeReservationsCount) {
        this.activeReservationsCount = activeReservationsCount;
    }
}
