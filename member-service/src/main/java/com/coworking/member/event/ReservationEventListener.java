package com.coworking.member.event;

import com.coworking.member.exception.ResourceNotFoundException;
import com.coworking.member.model.Member;
import com.coworking.member.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationEventListener.class);
    
    @Autowired
    private MemberService memberService;
    
    @KafkaListener(topics = "reservation.created", groupId = "member-service-group")
    public void handleReservationCreated(ReservationCreatedEvent event) {
        try {
            logger.info("Received ReservationCreatedEvent: {}", event);
            
            Long memberId = event.getMemberId();
            Member member = memberService.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
            
            member.setActiveReservationsCount(member.getActiveReservationsCount() + 1);
            memberService.update(memberId, member);
            
            logger.info("Member {} now has {} active reservations, max allowed: {}", 
                    memberId, member.getActiveReservationsCount(), member.getMaxConcurrentBookings());
            
            if (member.getActiveReservationsCount() >= member.getMaxConcurrentBookings() && !member.isSuspended()) {
                logger.warn("Member {} reached quota limit. Suspending member.", memberId);
                memberService.updateSuspensionStatus(memberId, true);
            }
        } catch (Exception e) {
            logger.error("Error handling ReservationCreatedEvent: {}", event, e);
        }
    }
    
    @KafkaListener(topics = "reservation.status.changed", groupId = "member-service-group")
    public void handleReservationStatusChanged(ReservationStatusChangedEvent event) {
        try {
            logger.info("Received ReservationStatusChangedEvent: {}", event);
            
            if ("CANCELLED".equals(event.getNewStatus()) || "COMPLETED".equals(event.getNewStatus())) {
                Long memberId = event.getMemberId();
                Member member = memberService.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
                
                member.setActiveReservationsCount(Math.max(0, member.getActiveReservationsCount() - 1));
                memberService.update(memberId, member);
                
                logger.info("Member {} now has {} active reservations after status change", 
                        memberId, member.getActiveReservationsCount());
                
                if (member.isSuspended() && member.getActiveReservationsCount() < member.getMaxConcurrentBookings()) {
                    logger.info("Member {} is below quota. Unsuspending member.", memberId);
                    memberService.updateSuspensionStatus(memberId, false);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling ReservationStatusChangedEvent: {}", event, e);
        }
    }
}
