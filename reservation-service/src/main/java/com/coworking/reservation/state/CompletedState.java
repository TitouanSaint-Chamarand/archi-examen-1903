package com.coworking.reservation.state;

import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class CompletedState implements ReservationState {
    
    @Override
    public void cancel(Reservation reservation) {
        throw new BusinessRuleException("Cannot cancel a completed reservation");
    }
    
    @Override
    public void complete(Reservation reservation) {
        throw new BusinessRuleException("Reservation is already completed");
    }
    
    @Override
    public String getStateName() {
        return "COMPLETED";
    }
}
