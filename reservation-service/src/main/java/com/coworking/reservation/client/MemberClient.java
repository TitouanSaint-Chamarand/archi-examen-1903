package com.coworking.reservation.client;

import com.coworking.reservation.dto.MemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "member-service")
public interface MemberClient {
    
    @GetMapping("/api/members/{id}")
    MemberDTO getMemberById(@PathVariable("id") Long id);
    
    @GetMapping("/api/members/{id}/suspended")
    Map<String, Boolean> isSuspended(@PathVariable("id") Long id);
}
