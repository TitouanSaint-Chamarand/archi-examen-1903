package com.coworking.member.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(MemberEventPublisher.class);
    private static final String MEMBER_DELETED_TOPIC = "member.deleted";
    
    @Autowired
    private KafkaTemplate<String, MemberDeletedEvent> kafkaTemplate;
    
    public void publishMemberDeleted(MemberDeletedEvent event) {
        try {
            logger.info("Publishing MemberDeletedEvent for memberId: {}", event.getMemberId());
            kafkaTemplate.send(MEMBER_DELETED_TOPIC, event.getMemberId().toString(), event);
            logger.info("Successfully published MemberDeletedEvent: {}", event);
        } catch (Exception e) {
            logger.error("Error publishing MemberDeletedEvent: {}", event, e);
        }
    }
}
