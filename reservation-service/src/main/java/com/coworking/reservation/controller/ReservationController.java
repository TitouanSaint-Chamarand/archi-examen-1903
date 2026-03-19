package com.coworking.reservation.controller;

import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.repository.ReservationRepository;
import com.coworking.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation Management", description = "API de gestion des réservations avec validations cross-services et transitions d'état")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @PostMapping
    @Operation(summary = "Créer une nouvelle réservation", 
               description = "Crée une réservation après validation de la disponibilité de la salle et du statut du membre. Utilise le Builder Pattern pour les validations.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Réservation créée avec succès (statut CONFIRMED)"),
        @ApiResponse(responseCode = "400", description = "Validation échouée (salle indisponible, membre suspendu, ou créneaux invalides)")
    })
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        try {
            Reservation createdReservation = reservationService.createReservation(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    @Operation(summary = "Lister les réservations", description = "Récupère toutes les réservations avec filtres optionnels par salle ou membre")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des réservations récupérée avec succès")
    })
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long memberId) {
        
        List<Reservation> reservations;
        if (roomId != null) {
            reservations = reservationService.findByRoomId(roomId);
        } else if (memberId != null) {
            reservations = reservationService.findByMemberId(memberId);
        } else {
            reservations = reservationService.findAll();
        }
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une réservation par ID", description = "Récupère les détails d'une réservation spécifique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Réservation trouvée"),
        @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une réservation", 
               description = "Change le statut d'une réservation à CANCELLED. Utilise le State Pattern pour gérer la transition et publier un événement Kafka.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Réservation annulée avec succès"),
        @ApiResponse(responseCode = "400", description = "Transition interdite (réservation déjà complétée ou annulée)"),
        @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        try {
            Reservation cancelledReservation = reservationService.cancelReservation(id);
            return ResponseEntity.ok(cancelledReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Compléter une réservation", 
               description = "Change le statut d'une réservation à COMPLETED. Utilise le State Pattern pour gérer la transition et publier un événement Kafka.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Réservation complétée avec succès"),
        @ApiResponse(responseCode = "400", description = "Transition interdite (réservation déjà complétée ou annulée)"),
        @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    public ResponseEntity<Reservation> completeReservation(@PathVariable Long id) {
        try {
            Reservation completedReservation = reservationService.completeReservation(id);
            return ResponseEntity.ok(completedReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/member/{memberId}/active/count")
    @Operation(summary = "Compter les réservations actives d'un membre", 
               description = "Retourne le nombre de réservations CONFIRMED d'un membre (utilisé pour la gestion des quotas)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nombre de réservations actives récupéré")
    })
    public ResponseEntity<Long> countActiveReservationsForMember(@PathVariable Long memberId) {
        long count = reservationRepository.countByMemberIdAndStatus(memberId, ReservationStatus.CONFIRMED);
        return ResponseEntity.ok(count);
    }
}
