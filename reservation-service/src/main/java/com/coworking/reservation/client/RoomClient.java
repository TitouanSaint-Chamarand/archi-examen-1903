package com.coworking.reservation.client;

import com.coworking.reservation.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "room-service")
public interface RoomClient {
    
    @GetMapping("/api/rooms/{id}")
    RoomDTO getRoomById(@PathVariable("id") Long id);
    
    @GetMapping("/api/rooms/{id}/available")
    Map<String, Boolean> checkAvailability(@PathVariable("id") Long id);
    
    @GetMapping("/api/rooms/{id}/available/timeslot")
    Map<String, Boolean> checkAvailabilityForTimeSlot(
            @PathVariable("id") Long id,
            @RequestParam("startDateTime") String startDateTime,
            @RequestParam("endDateTime") String endDateTime
    );
}
