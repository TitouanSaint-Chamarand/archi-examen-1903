package com.coworking.reservation.service;

import com.coworking.reservation.builder.ReservationBuilder;
import com.coworking.reservation.client.MemberClient;
import com.coworking.reservation.client.RoomClient;
import com.coworking.reservation.event.ReservationEventPublisher;
import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.exception.ResourceNotFoundException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.repository.ReservationRepository;
import com.coworking.reservation.state.ConfirmedState;
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
    private ReservationBuilder reservationBuilder;

    @Mock
    private ReservationEventPublisher eventPublisher;

    @Mock
    private ConfirmedState confirmedState;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED
        );
        testReservation.setId(1L);
    }

    @Test
    @DisplayName("Should create reservation when room available and member not suspended")
    void testCreateReservationSuccess() {
        Reservation requestReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED
        );
        
        when(reservationBuilder.withRoomId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withMemberId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withTimeSlot(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationBuilder);
        when(reservationBuilder.build()).thenReturn(testReservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation createdReservation = reservationService.createReservation(requestReservation);

        assertThat(createdReservation).isNotNull();
        assertThat(createdReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationBuilder, times(1)).withRoomId(1L);
        verify(reservationBuilder, times(1)).withMemberId(1L);
        verify(reservationBuilder, times(1)).build();
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw exception when room is not available")
    void testCreateReservationRoomUnavailable() {
        Reservation requestReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED
        );
        
        when(reservationBuilder.withRoomId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withMemberId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withTimeSlot(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationBuilder);
        when(reservationBuilder.build()).thenThrow(new BusinessRuleException("Room is not available for the requested time slot"));

        assertThatThrownBy(() -> reservationService.createReservation(requestReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Room is not available");
    }

    @Test
    @DisplayName("Should throw exception when member is suspended")
    void testCreateReservationMemberSuspended() {
        Reservation requestReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED
        );
        
        when(reservationBuilder.withRoomId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withMemberId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withTimeSlot(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationBuilder);
        when(reservationBuilder.build()).thenThrow(new BusinessRuleException("Member is suspended and cannot make reservations"));

        assertThatThrownBy(() -> reservationService.createReservation(requestReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Member is suspended");
    }

    @Test
    @DisplayName("Should throw exception when room does not exist")
    void testCreateReservationRoomNotFound() {
        Reservation requestReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED
        );
        
        when(reservationBuilder.withRoomId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withMemberId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withTimeSlot(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationBuilder);
        when(reservationBuilder.build()).thenThrow(new BusinessRuleException("Room not found or unavailable: Room not found"));

        assertThatThrownBy(() -> reservationService.createReservation(requestReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Room not found or unavailable");
    }

    @Test
    @DisplayName("Should throw exception when member does not exist")
    void testCreateReservationMemberNotFound() {
        Reservation requestReservation = new Reservation(
            1L, 1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED
        );
        
        when(reservationBuilder.withRoomId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withMemberId(1L)).thenReturn(reservationBuilder);
        when(reservationBuilder.withTimeSlot(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationBuilder);
        when(reservationBuilder.build()).thenThrow(new BusinessRuleException("Member not found or suspended: Member not found"));

        assertThatThrownBy(() -> reservationService.createReservation(requestReservation))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Member not found or suspended");
    }

    @Test
    @DisplayName("Should cancel reservation")
    void testCancelReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        testReservation.changeState(confirmedState);

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
        testReservation.changeState(confirmedState);

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
