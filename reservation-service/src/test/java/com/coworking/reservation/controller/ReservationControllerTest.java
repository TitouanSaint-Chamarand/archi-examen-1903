package com.coworking.reservation.controller;

import com.coworking.reservation.model.Reservation;
import com.coworking.reservation.model.ReservationStatus;
import com.coworking.reservation.service.ReservationService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Reservation Controller Tests")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testReservation = new Reservation(
            1L, 1L,
            LocalDateTime.of(2025, 1, 15, 10, 0),
            LocalDateTime.of(2025, 1, 15, 12, 0),
            ReservationStatus.CONFIRMED
        );
        testReservation.setId(1L);
    }

    @Test
    @DisplayName("POST /api/reservations - Should create reservation with validations OK (201)")
    void testCreateReservationSuccess() throws Exception {
        when(reservationService.createReservation(any(Reservation.class))).thenReturn(testReservation);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1)).createReservation(any(Reservation.class));
    }

    @Test
    @DisplayName("POST /api/reservations - Should return 400 when room unavailable")
    void testCreateReservationRoomUnavailable() throws Exception {
        when(reservationService.createReservation(any(Reservation.class)))
            .thenThrow(new RuntimeException("Room is not available"));

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().isBadRequest());

        verify(reservationService, times(1)).createReservation(any(Reservation.class));
    }

    @Test
    @DisplayName("POST /api/reservations - Should return 400 when member suspended")
    void testCreateReservationMemberSuspended() throws Exception {
        when(reservationService.createReservation(any(Reservation.class)))
            .thenThrow(new RuntimeException("Member is suspended"));

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().isBadRequest());

        verify(reservationService, times(1)).createReservation(any(Reservation.class));
    }

    @Test
    @DisplayName("GET /api/reservations - Should list all reservations (200)")
    void testGetAllReservations() throws Exception {
        Reservation res2 = new Reservation(
            2L, 1L,
            LocalDateTime.of(2025, 1, 16, 14, 0),
            LocalDateTime.of(2025, 1, 16, 16, 0),
            ReservationStatus.CONFIRMED
        );
        res2.setId(2L);
        
        List<Reservation> reservations = Arrays.asList(testReservation, res2);
        when(reservationService.findAll()).thenReturn(reservations);

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(reservationService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/reservations?roomId=1 - Should filter by room (200)")
    void testGetReservationsByRoomId() throws Exception {
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationService.findByRoomId(1L)).thenReturn(reservations);

        mockMvc.perform(get("/api/reservations?roomId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roomId").value(1));

        verify(reservationService, times(1)).findByRoomId(1L);
    }

    @Test
    @DisplayName("GET /api/reservations?memberId=1 - Should filter by member (200)")
    void testGetReservationsByMemberId() throws Exception {
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationService.findByMemberId(1L)).thenReturn(reservations);

        mockMvc.perform(get("/api/reservations?memberId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].memberId").value(1));

        verify(reservationService, times(1)).findByMemberId(1L);
    }

    @Test
    @DisplayName("GET /api/reservations/{id} - Should get a reservation (200)")
    void testGetReservationById() throws Exception {
        when(reservationService.findById(1L)).thenReturn(Optional.of(testReservation));

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("PATCH /api/reservations/{id}/cancel - Should cancel reservation (200)")
    void testCancelReservation() throws Exception {
        testReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.cancelReservation(1L)).thenReturn(testReservation);

        mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(reservationService, times(1)).cancelReservation(1L);
    }

    @Test
    @DisplayName("PATCH /api/reservations/{id}/complete - Should complete reservation (200)")
    void testCompleteReservation() throws Exception {
        testReservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationService.completeReservation(1L)).thenReturn(testReservation);

        mockMvc.perform(patch("/api/reservations/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(reservationService, times(1)).completeReservation(1L);
    }
}
