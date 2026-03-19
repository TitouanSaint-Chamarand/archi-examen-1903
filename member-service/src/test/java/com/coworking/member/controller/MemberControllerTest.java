package com.coworking.member.controller;

import com.coworking.member.model.Member;
import com.coworking.member.model.SubscriptionType;
import com.coworking.member.service.MemberService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Member Controller Tests")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("John Doe", "john@example.com", SubscriptionType.PRO);
        testMember.setId(1L);
    }

    @Test
    @DisplayName("POST /api/members - Should create a member (201)")
    void testCreateMember() throws Exception {
        when(memberService.create(any(Member.class))).thenReturn(testMember);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMember)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(memberService, times(1)).create(any(Member.class));
    }

    @Test
    @DisplayName("GET /api/members - Should list all members (200)")
    void testGetAllMembers() throws Exception {
        Member member2 = new Member("Jane Smith", "jane@example.com", SubscriptionType.BASIC);
        member2.setId(2L);
        
        List<Member> members = Arrays.asList(testMember, member2);
        when(memberService.findAll()).thenReturn(members);

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[1].fullName").value("Jane Smith"));

        verify(memberService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/members/{id} - Should get a member (200)")
    void testGetMemberById() throws Exception {
        when(memberService.findById(1L)).thenReturn(Optional.of(testMember));

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(memberService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/members/{id} - Should return 404 when member not found")
    void testGetMemberByIdNotFound() throws Exception {
        when(memberService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/members/999"))
                .andExpect(status().isNotFound());

        verify(memberService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("GET /api/members/{id}/suspended - Should check if suspended (200)")
    void testIsSuspended() throws Exception {
        when(memberService.isSuspended(1L)).thenReturn(false);

        mockMvc.perform(get("/api/members/1/suspended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false));

        verify(memberService, times(1)).isSuspended(1L);
    }

    @Test
    @DisplayName("PUT /api/members/{id} - Should update a member (200)")
    void testUpdateMember() throws Exception {
        Member updatedMember = new Member("John Updated", "john.updated@example.com", SubscriptionType.ENTERPRISE);
        updatedMember.setId(1L);
        
        when(memberService.update(eq(1L), any(Member.class))).thenReturn(updatedMember);

        mockMvc.perform(put("/api/members/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(memberService, times(1)).update(eq(1L), any(Member.class));
    }

    @Test
    @DisplayName("PATCH /api/members/{id}/suspension - Should change suspension status (200)")
    void testUpdateSuspension() throws Exception {
        testMember.setSuspended(true);
        when(memberService.updateSuspensionStatus(1L, true)).thenReturn(testMember);

        mockMvc.perform(patch("/api/members/1/suspension")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"suspended\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true));

        verify(memberService, times(1)).updateSuspensionStatus(1L, true);
    }

    @Test
    @DisplayName("DELETE /api/members/{id} - Should delete a member (204)")
    void testDeleteMember() throws Exception {
        doNothing().when(memberService).delete(1L);

        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isNoContent());

        verify(memberService, times(1)).delete(1L);
    }
}
