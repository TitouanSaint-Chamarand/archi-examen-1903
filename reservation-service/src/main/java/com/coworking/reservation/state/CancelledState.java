package com.coworking.reservation.state;

import com.coworking.reservation.exception.BusinessRuleException;
import com.coworking.reservation.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class CancelledState implements ReservationState {
    
    @Override
    public void cancel(Reservation reservation) {
        throw new BusinessRuleException("Reservation is already cancelled");
    }
    
    @Override
    public void complete(Reservation reservation) {
        throw new BusinessRuleException("Cannot complete a cancelled reservation");
    }
    
    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}
