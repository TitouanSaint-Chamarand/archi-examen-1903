package com.coworking.room.service;

import com.coworking.room.exception.ResourceNotFoundException;
import com.coworking.room.model.Room;
import com.coworking.room.model.RoomType;
import com.coworking.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Room Service Tests")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room("Test Room", "Paris", 10, 
                           RoomType.MEETING_ROOM, new BigDecimal("25.00"));
        testRoom.setId(1L);
    }

    @Test
    @DisplayName("Should create a room")
    void testCreateRoom() {
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room createdRoom = roomService.create(testRoom);

        assertThat(createdRoom).isNotNull();
        assertThat(createdRoom.getName()).isEqualTo("Test Room");
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Should update a room")
    void testUpdateRoom() {
        Room updatedDetails = new Room("Updated Room", "Lyon", 15, 
                                      RoomType.PRIVATE_OFFICE, new BigDecimal("35.00"));
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room updatedRoom = roomService.update(1L, updatedDetails);

        assertThat(updatedRoom).isNotNull();
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent room")
    void testUpdateNonExistentRoom() {
        Room updatedDetails = new Room("Updated Room", "Lyon", 15, 
                                      RoomType.PRIVATE_OFFICE, new BigDecimal("35.00"));
        
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.update(999L, updatedDetails))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Room not found with id: 999");
    }

    @Test
    @DisplayName("Should delete a room")
    void testDeleteRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        doNothing().when(roomRepository).delete(any(Room.class));

        roomService.delete(1L);

        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).delete(testRoom);
    }

    @Test
    @DisplayName("Should check availability")
    void testCheckAvailability() {
        testRoom.setAvailable(true);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        boolean isAvailable = roomService.checkAvailability(1L);

        assertThat(isAvailable).isTrue();
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should update availability")
    void testUpdateAvailability() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room updatedRoom = roomService.updateAvailability(1L, false);

        assertThat(updatedRoom).isNotNull();
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Should throw exception when room not found")
    void testRoomNotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.checkAvailability(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Room not found with id: 999");
    }
}
