package com.coworking.reservation.state;

import com.coworking.reservation.model.Reservation;

public interface ReservationState {
    
    void cancel(Reservation reservation);
    
    void complete(Reservation reservation);
    
    String getStateName();
}
