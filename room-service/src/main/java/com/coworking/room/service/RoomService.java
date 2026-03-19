package com.coworking.room.service;

import com.coworking.room.client.ReservationClient;
import com.coworking.room.event.RoomDeletedEvent;
import com.coworking.room.event.RoomEventPublisher;
import com.coworking.room.exception.ResourceNotFoundException;
import com.coworking.room.model.Room;
import com.coworking.room.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomEventPublisher roomEventPublisher;
    
    @Autowired
    private ReservationClient reservationClient;
    
    public Room create(Room room) {
        return roomRepository.save(room);
    }
    
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }
    
    public List<Room> findAll() {
        return roomRepository.findAll();
    }
    
    public List<Room> findByCity(String city) {
        return roomRepository.findByCity(city);
    }
    
    public List<Room> findByAvailable(boolean available) {
        return roomRepository.findByAvailable(available);
    }
    
    public Room update(Long id, Room roomDetails) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        
        room.setName(roomDetails.getName());
        room.setCity(roomDetails.getCity());
        room.setCapacity(roomDetails.getCapacity());
        room.setType(roomDetails.getType());
        room.setHourlyRate(roomDetails.getHourlyRate());
        room.setAvailable(roomDetails.isAvailable());
        
        return roomRepository.save(room);
    }
    
    public Room updateAvailability(Long id, boolean available) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        
        room.setAvailable(available);
        return roomRepository.save(room);
    }
    
    public boolean checkAvailability(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return room.isAvailable();
    }
    
    public boolean isAvailableForTimeSlot(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        
        if (!room.isAvailable()) {
            return false;
        }
        
        try {
            Boolean noOverlap = reservationClient.checkOverlap(id, startDateTime.toString(), endDateTime.toString());
            return noOverlap != null && noOverlap;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void delete(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        roomRepository.delete(room);
        roomEventPublisher.publishRoomDeleted(new RoomDeletedEvent(room.getId(), room.getName(), room.getCity()));
    }
}
