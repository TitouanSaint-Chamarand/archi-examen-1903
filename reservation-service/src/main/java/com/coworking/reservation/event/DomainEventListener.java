package com.coworking.reservation.event;

import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DomainEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DomainEventListener.class);
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @KafkaListener(topics = "room.deleted", groupId = "reservation-service-group")
    public void handleRoomDeleted(RoomDeletedEvent event) {
        try {
            logger.info("Received RoomDeletedEvent: {}", event);
            
            List<Reservation> confirmedReservations = reservationRepository.findByRoomIdAndStatus(
                    event.getRoomId(), ReservationStatus.CONFIRMED);
            
            logger.info("Found {} CONFIRMED reservations for deleted room {}", 
                    confirmedReservations.size(), event.getRoomId());
            
            for (Reservation reservation : confirmedReservations) {
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                logger.info("Cancelled reservation {} due to room deletion", reservation.getId());
            }
            
            logger.info("Successfully cancelled all CONFIRMED reservations for room {}", event.getRoomId());
        } catch (Exception e) {
            logger.error("Error handling RoomDeletedEvent: {}", event, e);
        }
    }
    
    @KafkaListener(topics = "member.deleted", groupId = "reservation-service-group")
    public void handleMemberDeleted(MemberDeletedEvent event) {
        try {
            logger.info("Received MemberDeletedEvent: {}", event);
            
            List<Reservation> memberReservations = reservationRepository.findByMemberId(event.getMemberId());
            
            logger.info("Found {} reservations for deleted member {}", 
                    memberReservations.size(), event.getMemberId());
            
            reservationRepository.deleteAll(memberReservations);
            
            logger.info("Successfully deleted all reservations for member {}", event.getMemberId());
        } catch (Exception e) {
            logger.error("Error handling MemberDeletedEvent: {}", event, e);
        }
    }
}
