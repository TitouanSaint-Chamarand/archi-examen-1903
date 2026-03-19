package com.coworking.reservation.repository;

import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Reservation Repository Tests")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("Should save and retrieve a reservation")
    void testSaveAndRetrieveReservation() {
        Reservation reservation = new Reservation(
            1L, 1L, 
            LocalDateTime.now(), 
            LocalDateTime.now().plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        Reservation savedReservation = reservationRepository.save(reservation);
        
        assertThat(savedReservation.getId()).isNotNull();
        assertThat(savedReservation.getRoomId()).isEqualTo(1L);
        assertThat(savedReservation.getMemberId()).isEqualTo(1L);
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should find reservations by roomId")
    void testFindByRoomId() {
        Reservation res1 = new Reservation(
            1L, 1L, 
            LocalDateTime.now(), 
            LocalDateTime.now().plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        Reservation res2 = new Reservation(
            1L, 2L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        Reservation res3 = new Reservation(
            2L, 1L, 
            LocalDateTime.now().plusDays(2), 
            LocalDateTime.now().plusDays(2).plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        reservationRepository.save(res1);
        reservationRepository.save(res2);
        reservationRepository.save(res3);
        
        List<Reservation> room1Reservations = reservationRepository.findByRoomId(1L);
        
        assertThat(room1Reservations).hasSize(2);
        assertThat(room1Reservations).extracting(Reservation::getRoomId).containsOnly(1L);
    }

    @Test
    @DisplayName("Should find reservations by memberId")
    void testFindByMemberId() {
        Reservation res1 = new Reservation(
            1L, 1L, 
            LocalDateTime.now(), 
            LocalDateTime.now().plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        Reservation res2 = new Reservation(
            2L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        Reservation res3 = new Reservation(
            1L, 2L, 
            LocalDateTime.now().plusDays(2), 
            LocalDateTime.now().plusDays(2).plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        reservationRepository.save(res1);
        reservationRepository.save(res2);
        reservationRepository.save(res3);
        
        List<Reservation> member1Reservations = reservationRepository.findByMemberId(1L);
        
        assertThat(member1Reservations).hasSize(2);
        assertThat(member1Reservations).extracting(Reservation::getMemberId).containsOnly(1L);
    }

    @Test
    @DisplayName("Should find reservations by status CONFIRMED")
    void testFindByStatusConfirmed() {
        Reservation res1 = new Reservation(
            1L, 1L, 
            LocalDateTime.now(), 
            LocalDateTime.now().plusHours(2), 
            ReservationStatus.CONFIRMED
        );
        
        Reservation res2 = new Reservation(
            2L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2), 
            ReservationStatus.CANCELLED
        );
        
        reservationRepository.save(res1);
        reservationRepository.save(res2);
        
        List<Reservation> confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        
        assertThat(confirmedReservations).hasSize(1);
        assertThat(confirmedReservations.get(0).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should find reservations by status CANCELLED")
    void testFindByStatusCancelled() {
        Reservation res1 = new Reservation(
            1L, 1L, 
            LocalDateTime.now(), 
            LocalDateTime.now().plusHours(2), 
            ReservationStatus.CANCELLED
        );
        
        reservationRepository.save(res1);
        
        List<Reservation> cancelledReservations = reservationRepository.findByStatus(ReservationStatus.CANCELLED);
        
        assertThat(cancelledReservations).hasSize(1);
        assertThat(cancelledReservations.get(0).getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should find reservations by status COMPLETED")
    void testFindByStatusCompleted() {
        Reservation res1 = new Reservation(
            1L, 1L, 
            LocalDateTime.now().minusDays(1), 
            LocalDateTime.now().minusDays(1).plusHours(2), 
            ReservationStatus.COMPLETED
        );
        
        reservationRepository.save(res1);
        
        List<Reservation> completedReservations = reservationRepository.findByStatus(ReservationStatus.COMPLETED);
        
        assertThat(completedReservations).hasSize(1);
        assertThat(completedReservations.get(0).getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }
}
