package com.coworking.reservation.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationEventPublisher.class);
    private static final String RESERVATION_CREATED_TOPIC = "reservation.created";
    private static final String RESERVATION_STATUS_CHANGED_TOPIC = "reservation.status.changed";
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishReservationCreated(ReservationCreatedEvent event) {
        try {
            logger.info("Publishing ReservationCreatedEvent for reservationId: {}", event.getReservationId());
            kafkaTemplate.send(RESERVATION_CREATED_TOPIC, event.getReservationId().toString(), event);
            logger.info("Successfully published ReservationCreatedEvent: {}", event);
        } catch (Exception e) {
            logger.error("Error publishing ReservationCreatedEvent: {}", event, e);
        }
    }
    
    public void publishReservationStatusChanged(ReservationStatusChangedEvent event) {
        try {
            logger.info("Publishing ReservationStatusChangedEvent for reservationId: {}, status: {} -> {}", 
                    event.getReservationId(), event.getPreviousStatus(), event.getNewStatus());
            kafkaTemplate.send(RESERVATION_STATUS_CHANGED_TOPIC, event.getReservationId().toString(), event);
            logger.info("Successfully published ReservationStatusChangedEvent: {}", event);
        } catch (Exception e) {
            logger.error("Error publishing ReservationStatusChangedEvent: {}", event, e);
        }
    }
}
