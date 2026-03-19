package com.coworking.reservation.state;

import com.coworking.reservation.event.ReservationEventPublisher;
import com.coworking.reservation.event.ReservationStatusChangedEvent;
import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class ConfirmedState implements ReservationState {
    
    private final ReservationEventPublisher eventPublisher;
    private final CompletedState completedState;
    private final CancelledState cancelledState;
    
    public ConfirmedState(ReservationEventPublisher eventPublisher, 
                          CompletedState completedState,
                          CancelledState cancelledState) {
        this.eventPublisher = eventPublisher;
        this.completedState = completedState;
        this.cancelledState = cancelledState;
    }
    
    @Override
    public void cancel(Reservation reservation) {
        ReservationStatus previousStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.changeState(cancelledState);
        
        eventPublisher.publishReservationStatusChanged(new ReservationStatusChangedEvent(
            reservation.getId(),
            reservation.getMemberId(),
            previousStatus.name(),
            ReservationStatus.CANCELLED.name()
        ));
    }
    
    @Override
    public void complete(Reservation reservation) {
        ReservationStatus previousStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.changeState(completedState);
        
        eventPublisher.publishReservationStatusChanged(new ReservationStatusChangedEvent(
            reservation.getId(),
            reservation.getMemberId(),
            previousStatus.name(),
            ReservationStatus.COMPLETED.name()
        ));
    }
    
    @Override
    public String getStateName() {
        return "CONFIRMED";
    }
}
