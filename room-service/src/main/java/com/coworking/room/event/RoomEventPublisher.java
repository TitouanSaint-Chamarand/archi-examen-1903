package com.coworking.room.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoomEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomEventPublisher.class);
    private static final String ROOM_DELETED_TOPIC = "room.deleted";
    
    @Autowired
    private KafkaTemplate<String, RoomDeletedEvent> kafkaTemplate;
    
    public void publishRoomDeleted(RoomDeletedEvent event) {
        try {
            logger.info("Publishing RoomDeletedEvent for roomId: {}", event.getRoomId());
            kafkaTemplate.send(ROOM_DELETED_TOPIC, event.getRoomId().toString(), event);
            logger.info("Successfully published RoomDeletedEvent: {}", event);
        } catch (Exception e) {
            logger.error("Error publishing RoomDeletedEvent: {}", event, e);
        }
    }
}
