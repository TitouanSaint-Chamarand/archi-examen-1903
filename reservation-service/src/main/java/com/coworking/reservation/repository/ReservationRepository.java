package com.coworking.reservation.repository;

import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByRoomId(Long roomId);
    
    List<Reservation> findByMemberId(Long memberId);
    
    List<Reservation> findByStatus(ReservationStatus status);
}
