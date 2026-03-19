package com.coworking.room.controller;

import com.coworking.room.model.Room;
import com.coworking.room.model.RoomType;
import com.coworking.room.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Room Controller Tests")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room("Test Room", "Paris", 10, 
                           RoomType.MEETING_ROOM, new BigDecimal("25.00"));
        testRoom.setId(1L);
    }

    @Test
    @DisplayName("POST /api/rooms - Should create a room (201)")
    void testCreateRoom() throws Exception {
        when(roomService.create(any(Room.class))).thenReturn(testRoom);

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Room"))
                .andExpect(jsonPath("$.city").value("Paris"));

        verify(roomService, times(1)).create(any(Room.class));
    }

    @Test
    @DisplayName("GET /api/rooms - Should list all rooms (200)")
    void testGetAllRooms() throws Exception {
        Room room2 = new Room("Room 2", "Lyon", 8, 
                             RoomType.OPEN_SPACE, new BigDecimal("20.00"));
        room2.setId(2L);
        
        List<Room> rooms = Arrays.asList(testRoom, room2);
        when(roomService.findAll()).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Test Room"))
                .andExpect(jsonPath("$[1].name").value("Room 2"));

        verify(roomService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/rooms/{id} - Should get a room (200)")
    void testGetRoomById() throws Exception {
        when(roomService.findById(1L)).thenReturn(Optional.of(testRoom));

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Room"));

        verify(roomService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/rooms/{id} - Should return 404 when room not found")
    void testGetRoomByIdNotFound() throws Exception {
        when(roomService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rooms/999"))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("GET /api/rooms/{id}/available - Should check availability (200)")
    void testCheckAvailability() throws Exception {
        when(roomService.checkAvailability(1L)).thenReturn(true);

        mockMvc.perform(get("/api/rooms/1/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(roomService, times(1)).checkAvailability(1L);
    }

    @Test
    @DisplayName("PUT /api/rooms/{id} - Should update a room (200)")
    void testUpdateRoom() throws Exception {
        Room updatedRoom = new Room("Updated Room", "Lyon", 15, 
                                   RoomType.PRIVATE_OFFICE, new BigDecimal("35.00"));
        updatedRoom.setId(1L);
        
        when(roomService.update(eq(1L), any(Room.class))).thenReturn(updatedRoom);

        mockMvc.perform(put("/api/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRoom)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Room"))
                .andExpect(jsonPath("$.city").value("Lyon"));

        verify(roomService, times(1)).update(eq(1L), any(Room.class));
    }

    @Test
    @DisplayName("PATCH /api/rooms/{id}/availability - Should change availability (200)")
    void testUpdateAvailability() throws Exception {
        testRoom.setAvailable(false);
        when(roomService.updateAvailability(1L, false)).thenReturn(testRoom);

        mockMvc.perform(patch("/api/rooms/1/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(roomService, times(1)).updateAvailability(1L, false);
    }

    @Test
    @DisplayName("DELETE /api/rooms/{id} - Should delete a room (204)")
    void testDeleteRoom() throws Exception {
        doNothing().when(roomService).delete(1L);

        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isNoContent());

        verify(roomService, times(1)).delete(1L);
    }
}
