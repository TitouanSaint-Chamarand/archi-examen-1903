package com.coworking.reservation.controller;

import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        try {
            Reservation createdReservation = reservationService.createReservation(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
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
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        try {
            Reservation cancelledReservation = reservationService.cancelReservation(id);
            return ResponseEntity.ok(cancelledReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Reservation> completeReservation(@PathVariable Long id) {
        try {
            Reservation completedReservation = reservationService.completeReservation(id);
            return ResponseEntity.ok(completedReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
