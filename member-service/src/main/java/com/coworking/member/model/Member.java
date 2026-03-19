package com.coworking.member.model;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType subscriptionType;
    
    @Column(nullable = false)
    private boolean suspended = false;
    
    @Column(nullable = false)
    private Integer maxConcurrentBookings;
    
    @Column(nullable = false)
    private int activeReservationsCount = 0;
    
    public Member() {
    }
    
    public Member(String fullName, String email, SubscriptionType subscriptionType) {
        this.fullName = fullName;
        this.email = email;
        this.subscriptionType = subscriptionType;
        this.maxConcurrentBookings = subscriptionType.getMaxConcurrentBookings();
        this.suspended = false;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }
    
    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
        this.maxConcurrentBookings = subscriptionType.getMaxConcurrentBookings();
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
