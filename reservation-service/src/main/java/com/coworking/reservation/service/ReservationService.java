package com.coworking.reservation.service;

import com.coworking.reservation.client.MemberClient;
import com.coworking.reservation.client.RoomClient;
import com.coworking.reservation.event.ReservationCreatedEvent;
import com.coworking.reservation.event.ReservationEventPublisher;
import com.coworking.reservation.event.ReservationStatusChangedEvent;
import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.exception.ResourceNotFoundException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private RoomClient roomClient;
    
    @Autowired
    private MemberClient memberClient;
    
    @Autowired
    private ReservationEventPublisher reservationEventPublisher;
    
    public Reservation createReservation(Reservation reservation) {
        try {
            Map<String, Boolean> roomAvailability = roomClient.checkAvailability(reservation.getRoomId());
            if (!roomAvailability.get("available")) {
                throw new BusinessRuleException("Room is not available for the requested time slot");
            }
        } catch (Exception e) {
            throw new BusinessRuleException("Room not found or unavailable: " + e.getMessage());
        }
        
        try {
            Map<String, Boolean> memberSuspension = memberClient.isSuspended(reservation.getMemberId());
            if (memberSuspension.get("suspended")) {
                throw new BusinessRuleException("Member is suspended and cannot make reservations");
            }
        } catch (Exception e) {
            throw new BusinessRuleException("Member not found or suspended: " + e.getMessage());
        }
        
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation savedReservation = reservationRepository.save(reservation);
        
        reservationEventPublisher.publishReservationCreated(new ReservationCreatedEvent(
                savedReservation.getId(),
                savedReservation.getMemberId(),
                savedReservation.getRoomId(),
                savedReservation.getStartDateTime(),
                savedReservation.getEndDateTime()
        ));
        
        return savedReservation;
    }
    
    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }
    
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
    
    public List<Reservation> findByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }
    
    public List<Reservation> findByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }
    
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }
    
    public Reservation cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        
        ReservationStatus previousStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updatedReservation = reservationRepository.save(reservation);
        
        reservationEventPublisher.publishReservationStatusChanged(new ReservationStatusChangedEvent(
                updatedReservation.getId(),
                updatedReservation.getMemberId(),
                previousStatus.name(),
                ReservationStatus.CANCELLED.name()
        ));
        
        return updatedReservation;
    }
    
    public Reservation completeReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        
        ReservationStatus previousStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation updatedReservation = reservationRepository.save(reservation);
        
        reservationEventPublisher.publishReservationStatusChanged(new ReservationStatusChangedEvent(
                updatedReservation.getId(),
                updatedReservation.getMemberId(),
                previousStatus.name(),
                ReservationStatus.COMPLETED.name()
        ));
        
        return updatedReservation;
    }
}
