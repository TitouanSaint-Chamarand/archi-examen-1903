package com.coworking.reservation.service;

import com.coworking.reservation.client.MemberClient;
import com.coworking.reservation.client.RoomClient;
import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.exception.ResourceNotFoundException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reservation Service Tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomClient roomClient;

    @Mock
    private MemberClient memberClient;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2),
            ReservationStatus.CONFIRMED
        );
        testReservation.setId(1L);
    }

    @Test
    @DisplayName("Should create reservation when room available and member not suspended")
    void testCreateReservationSuccess() {
        Map<String, Boolean> roomAvailability = new HashMap<>();
        roomAvailability.put("available", true);
        
        Map<String, Boolean> memberSuspension = new HashMap<>();
        memberSuspension.put("suspended", false);
        
        when(roomClient.checkAvailability(1L)).thenReturn(roomAvailability);
        when(memberClient.isSuspended(1L)).thenReturn(memberSuspension);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation createdReservation = reservationService.createReservation(testReservation);

        assertThat(createdReservation).isNotNull();
        assertThat(createdReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(roomClient, times(1)).checkAvailability(1L);
        verify(memberClient, times(1)).isSuspended(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw exception when room is not available")
    void testCreateReservationRoomUnavailable() {
        Map<String, Boolean> roomAvailability = new HashMap<>();
        roomAvailability.put("available", false);
        
        when(roomClient.checkAvailability(1L)).thenReturn(roomAvailability);

        assertThatThrownBy(() -> reservationService.createReservation(testReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Room is not available");
    }

    @Test
    @DisplayName("Should throw exception when member is suspended")
    void testCreateReservationMemberSuspended() {
        Map<String, Boolean> roomAvailability = new HashMap<>();
        roomAvailability.put("available", true);
        
        Map<String, Boolean> memberSuspension = new HashMap<>();
        memberSuspension.put("suspended", true);
        
        when(roomClient.checkAvailability(1L)).thenReturn(roomAvailability);
        when(memberClient.isSuspended(1L)).thenReturn(memberSuspension);

        assertThatThrownBy(() -> reservationService.createReservation(testReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Member is suspended");
    }

    @Test
    @DisplayName("Should throw exception when room does not exist")
    void testCreateReservationRoomNotFound() {
        when(roomClient.checkAvailability(1L)).thenThrow(new RuntimeException("Room not found"));

        assertThatThrownBy(() -> reservationService.createReservation(testReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Room not found or unavailable");
    }

    @Test
    @DisplayName("Should throw exception when member does not exist")
    void testCreateReservationMemberNotFound() {
        Map<String, Boolean> roomAvailability = new HashMap<>();
        roomAvailability.put("available", true);
        
        when(roomClient.checkAvailability(1L)).thenReturn(roomAvailability);
        when(memberClient.isSuspended(1L)).thenThrow(new RuntimeException("Member not found"));

        assertThatThrownBy(() -> reservationService.createReservation(testReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Member not found or suspended");
    }

    @Test
    @DisplayName("Should cancel reservation")
    void testCancelReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation cancelledReservation = reservationService.cancelReservation(1L);

        assertThat(cancelledReservation).isNotNull();
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should complete reservation")
    void testCompleteReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation completedReservation = reservationService.completeReservation(1L);

        assertThat(completedReservation).isNotNull();
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw exception when reservation not found")
    void testReservationNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Reservation not found with id: 999");
    }
}
