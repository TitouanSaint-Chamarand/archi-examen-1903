package com.coworking.room.controller;

import com.coworking.room.model.Room;
import com.coworking.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room createdRoom = roomService.create(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }
    
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean available) {
        
        List<Room> rooms;
        if (city != null) {
            rooms = roomService.findByCity(city);
        } else if (available != null) {
            rooms = roomService.findByAvailable(available);
        } else {
            rooms = roomService.findAll();
        }
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/available")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@PathVariable Long id) {
        try {
            boolean available = roomService.checkAvailability(id);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        try {
            Room updatedRoom = roomService.update(id, room);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Room> updateAvailability(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> availabilityUpdate) {
        try {
            boolean available = availabilityUpdate.get("available");
            Room updatedRoom = roomService.updateAvailability(id, available);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        try {
            roomService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
