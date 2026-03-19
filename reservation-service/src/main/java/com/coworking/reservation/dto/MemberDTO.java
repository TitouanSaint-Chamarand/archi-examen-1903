package com.coworking.reservation.dto;

public class MemberDTO {
    
    private Long id;
    private String fullName;
    private boolean suspended;
    
    public MemberDTO() {
    }
    
    public MemberDTO(Long id, String fullName, boolean suspended) {
        this.id = id;
        this.fullName = fullName;
        this.suspended = suspended;
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
}
