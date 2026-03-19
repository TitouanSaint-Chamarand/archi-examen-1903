package com.coworking.room.event;

import java.time.LocalDateTime;

public class RoomDeletedEvent {
    
    private Long roomId;
    private String name;
    private String city;
    private LocalDateTime timestamp;
    
    public RoomDeletedEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public RoomDeletedEvent(Long roomId, String name, String city) {
        this.roomId = roomId;
        this.name = name;
        this.city = city;
        this.timestamp = LocalDateTime.now();
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "RoomDeletedEvent{" +
                "roomId=" + roomId +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
