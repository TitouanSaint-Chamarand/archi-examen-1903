package com.coworking.reservation.state;

import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CancelledStateTest {
    
    private CancelledState cancelledState;
    
    @BeforeEach
    void setUp() {
        cancelledState = new CancelledState();
    }
    
    @Test
    void cancel_ShouldThrowBusinessRuleException() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CANCELLED);
        reservation.setId(1L);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            cancelledState.cancel(reservation);
        });
        
        assertEquals("Reservation is already cancelled", exception.getMessage());
    }
    
    @Test
    void complete_ShouldThrowBusinessRuleException() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.CANCELLED);
        reservation.setId(1L);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            cancelledState.complete(reservation);
        });
        
        assertEquals("Cannot complete a cancelled reservation", exception.getMessage());
    }
    
    @Test
    void getStateName_ShouldReturnCancelled() {
        assertEquals("CANCELLED", cancelledState.getStateName());
    }
}
