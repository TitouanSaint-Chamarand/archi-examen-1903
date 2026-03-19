package com.coworking.reservation.state;

import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CompletedStateTest {
    
    private CompletedState completedState;
    
    @BeforeEach
    void setUp() {
        completedState = new CompletedState();
    }
    
    @Test
    void cancel_ShouldThrowBusinessRuleException() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.COMPLETED);
        reservation.setId(1L);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            completedState.cancel(reservation);
        });
        
        assertEquals("Cannot cancel a completed reservation", exception.getMessage());
    }
    
    @Test
    void complete_ShouldThrowBusinessRuleException() {
        Reservation reservation = new Reservation(1L, 1L, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(1).plusHours(2),
            ReservationStatus.COMPLETED);
        reservation.setId(1L);
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            completedState.complete(reservation);
        });
        
        assertEquals("Reservation is already completed", exception.getMessage());
    }
    
    @Test
    void getStateName_ShouldReturnCompleted() {
        assertEquals("COMPLETED", completedState.getStateName());
    }
}
