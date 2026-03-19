package com.coworking.room.repository;

import com.coworking.room.model.Room;
import com.coworking.room.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Room Repository Tests")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("Should save and retrieve a room")
    void testSaveAndRetrieveRoom() {
        Room room = new Room("Salle Innovation", "Paris", 10, 
                            RoomType.MEETING_ROOM, new BigDecimal("25.50"));
        
        Room savedRoom = roomRepository.save(room);
        
        assertThat(savedRoom.getId()).isNotNull();
        assertThat(savedRoom.getName()).isEqualTo("Salle Innovation");
        assertThat(savedRoom.getCity()).isEqualTo("Paris");
        assertThat(savedRoom.getCapacity()).isEqualTo(10);
        assertThat(savedRoom.getType()).isEqualTo(RoomType.MEETING_ROOM);
        assertThat(savedRoom.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Should find rooms by city")
    void testFindByCity() {
        Room room1 = new Room("Salle A", "Paris", 5, 
                             RoomType.OPEN_SPACE, new BigDecimal("15.00"));
        Room room2 = new Room("Salle B", "Lyon", 8, 
                             RoomType.MEETING_ROOM, new BigDecimal("20.00"));
        Room room3 = new Room("Salle C", "Paris", 12, 
                             RoomType.PRIVATE_OFFICE, new BigDecimal("30.00"));
        
        roomRepository.save(room1);
        roomRepository.save(room2);
        roomRepository.save(room3);
        
        List<Room> parisRooms = roomRepository.findByCity("Paris");
        
        assertThat(parisRooms).hasSize(2);
        assertThat(parisRooms).extracting(Room::getCity).containsOnly("Paris");
    }

    @Test
    @DisplayName("Should find rooms by availability")
    void testFindByAvailable() {
        Room room1 = new Room("Salle Available", "Paris", 5, 
                             RoomType.OPEN_SPACE, new BigDecimal("15.00"));
        room1.setAvailable(true);
        
        Room room2 = new Room("Salle Occupied", "Paris", 8, 
                             RoomType.MEETING_ROOM, new BigDecimal("20.00"));
        room2.setAvailable(false);
        
        roomRepository.save(room1);
        roomRepository.save(room2);
        
        List<Room> availableRooms = roomRepository.findByAvailable(true);
        
        assertThat(availableRooms).hasSize(1);
        assertThat(availableRooms.get(0).isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Should verify data persistence")
    void testDataPersistence() {
        Room room = new Room("Test Room", "Marseille", 6, 
                            RoomType.MEETING_ROOM, new BigDecimal("22.00"));
        
        Room savedRoom = roomRepository.save(room);
        Long roomId = savedRoom.getId();
        
        Optional<Room> retrievedRoom = roomRepository.findById(roomId);
        
        assertThat(retrievedRoom).isPresent();
        assertThat(retrievedRoom.get().getName()).isEqualTo("Test Room");
        assertThat(retrievedRoom.get().getCity()).isEqualTo("Marseille");
    }
}
