package com.coworking.reservation.service;

import com.coworking.reservation.builder.ReservationBuilder;
import com.coworking.reservation.client.MemberClient;
import com.coworking.reservation.client.RoomClient;
import com.coworking.reservation.event.ReservationCreatedEvent;
import com.coworking.reservation.event.ReservationEventPublisher;
import com.coworking.reservation.exception.ResourceNotFoundException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.repository.ReservationRepository;
import com.coworking.reservation.state.ConfirmedState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ReservationBuilder reservationBuilder;
    
    @Autowired
    private ReservationEventPublisher reservationEventPublisher;
    
    @Autowired
    private ConfirmedState confirmedState;
    
    public Reservation createReservation(Reservation reservationRequest) {
        Reservation reservation = reservationBuilder
            .withRoomId(reservationRequest.getRoomId())
            .withMemberId(reservationRequest.getMemberId())
            .withTimeSlot(reservationRequest.getStartDateTime(), reservationRequest.getEndDateTime())
            .build();
        
        Reservation savedReservation = reservationRepository.save(reservation);
        savedReservation.changeState(confirmedState);
        
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
        
        reservation.cancel();
        return reservationRepository.save(reservation);
    }
    
    public Reservation completeReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        
        reservation.complete();
        return reservationRepository.save(reservation);
    }
}
