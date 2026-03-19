package com.coworking.reservation.state;

import com.coworking.reservation.event.ReservationEventPublisher;
import com.coworking.reservation.event.ReservationStatusChangedEvent;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmedStateTest {
    
    @Mock
    private ReservationEventPublisher eventPublisher;
    
    @Mock
    private CompletedState completedState;
    
    @Mock
    private CancelledState cancelledState;
    
    private ConfirmedState confirmedState;
    
    @BeforeEach
    void setUp() {
        confirmedState = new ConfirmedState(eventPublisher, completedState, cancelledState);
    }
    
    @Test
    void cancel_ShouldChangeStatusToCancelled() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED);
        reservation.setId(1L);
        
        confirmedState.cancel(reservation);
        
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }
    
    @Test
    void cancel_ShouldChangeStateToCancelledState() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED);
        reservation.setId(1L);
        
        confirmedState.cancel(reservation);
        
        assertEquals(cancelledState, reservation.getCurrentState());
    }
    
    @Test
    void cancel_ShouldPublishStatusChangedEvent() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED);
        reservation.setId(1L);
        
        confirmedState.cancel(reservation);
        
        ArgumentCaptor<ReservationStatusChangedEvent> eventCaptor = 
            ArgumentCaptor.forClass(ReservationStatusChangedEvent.class);
        verify(eventPublisher, times(1)).publishReservationStatusChanged(eventCaptor.capture());
        
        ReservationStatusChangedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getReservationId());
        assertEquals(1L, event.getMemberId());
        assertEquals("CONFIRMED", event.getPreviousStatus());
        assertEquals("CANCELLED", event.getNewStatus());
    }
    
    @Test
    void complete_ShouldChangeStatusToCompleted() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED);
        reservation.setId(1L);
        
        confirmedState.complete(reservation);
        
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
    }
    
    @Test
    void complete_ShouldChangeStateToCompletedState() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED);
        reservation.setId(1L);
        
        confirmedState.complete(reservation);
        
        assertEquals(completedState, reservation.getCurrentState());
    }
    
    @Test
    void complete_ShouldPublishStatusChangedEvent() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CONFIRMED);
        reservation.setId(1L);
        
        confirmedState.complete(reservation);
        
        ArgumentCaptor<ReservationStatusChangedEvent> eventCaptor = 
            ArgumentCaptor.forClass(ReservationStatusChangedEvent.class);
        verify(eventPublisher, times(1)).publishReservationStatusChanged(eventCaptor.capture());
        
        ReservationStatusChangedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getReservationId());
        assertEquals(1L, event.getMemberId());
        assertEquals("CONFIRMED", event.getPreviousStatus());
        assertEquals("COMPLETED", event.getNewStatus());
    }
    
    @Test
    void getStateName_ShouldReturnConfirmed() {
        assertEquals("CONFIRMED", confirmedState.getStateName());
    }
}
