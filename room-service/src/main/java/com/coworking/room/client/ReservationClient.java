package com.coworking.room.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reservation-service")
public interface ReservationClient {
    
    @GetMapping("/api/reservations/check-overlap")
    Boolean checkOverlap(
            @RequestParam("roomId") Long roomId,
            @RequestParam("startDateTime") String startDateTime,
            @RequestParam("endDateTime") String endDateTime
    );
}
