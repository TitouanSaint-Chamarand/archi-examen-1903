package com.coworking.room.service;

import com.coworking.room.exception.ResourceNotFoundException;
import com.coworking.room.model.Room;
import com.coworking.room.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
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
    
    public void delete(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        roomRepository.delete(room);
    }
}
