package com.coworking.room.controller;

import com.coworking.room.model.Room;
import com.coworking.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Management", description = "API de gestion des salles de coworking")
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @PostMapping
    @Operation(summary = "Créer une nouvelle salle", description = "Crée une salle de coworking avec ses caractéristiques (nom, ville, capacité, type, tarif horaire)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Salle créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room createdRoom = roomService.create(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }
    
    @GetMapping
    @Operation(summary = "Lister toutes les salles", description = "Récupère la liste de toutes les salles avec filtres optionnels par ville ou disponibilité")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des salles récupérée avec succès")
    })
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
    @Operation(summary = "Récupérer une salle par ID", description = "Récupère les détails d'une salle spécifique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Salle trouvée"),
        @ApiResponse(responseCode = "404", description = "Salle non trouvée")
    })
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/available")
    @Operation(summary = "Vérifier la disponibilité d'une salle", description = "Vérifie si une salle est disponible pour réservation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut de disponibilité récupéré"),
        @ApiResponse(responseCode = "404", description = "Salle non trouvée")
    })
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@PathVariable Long id) {
        try {
            boolean available = roomService.checkAvailability(id);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/available/timeslot")
    @Operation(summary = "Vérifier la disponibilité d'une salle pour un créneau horaire", 
               description = "Vérifie si une salle est disponible pour un créneau horaire spécifique en tenant compte des réservations existantes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut de disponibilité récupéré"),
        @ApiResponse(responseCode = "404", description = "Salle non trouvée"),
        @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    })
    public ResponseEntity<Map<String, Boolean>> checkAvailabilityForTimeSlot(
            @PathVariable Long id,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime) {
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDateTime);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDateTime);
            
            boolean available = roomService.isAvailableForTimeSlot(id, start, end);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("available", false));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une salle", description = "Met à jour les informations d'une salle existante")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Salle mise à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Salle non trouvée")
    })
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        try {
            Room updatedRoom = roomService.update(id, room);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/availability")
    @Operation(summary = "Modifier la disponibilité d'une salle", description = "Change le statut de disponibilité d'une salle (disponible/indisponible)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disponibilité mise à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Salle non trouvée")
    })
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
    @Operation(summary = "Supprimer une salle", description = "Supprime une salle et publie un événement Kafka pour annuler les réservations associées")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Salle supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Salle non trouvée")
    })
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        try {
            roomService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
