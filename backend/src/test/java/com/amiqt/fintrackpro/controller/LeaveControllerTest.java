package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.model.dto.request.LeaveRequestDto;
import com.amiqt.fintrackpro.model.dto.response.LeaveResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.AuditLogService;
import com.amiqt.fintrackpro.service.LeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = LeaveController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveService leaveService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private com.amiqt.fintrackpro.security.JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Authentication mockAuth(String email) {
        User user = new User();
        user.setEmail(email);
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    private LeaveResponse sampleLeave() {
        return new LeaveResponse(
                1L, 1L, "Noor Amin", LeaveType.ANNUAL,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2),
                2, "Holiday", LeaveStatus.PENDING, null, LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/v1/leaves — should return 200 with paginated leaves")
    void getAllLeavesTest() throws Exception {
        Page<LeaveResponse> page = new PageImpl<>(List.of(sampleLeave()));
        when(leaveService.getAllLeaves(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].leaveType").value("ANNUAL"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/leaves/pending — should return 200 with pending leaves only")
    void getPendingLeavesTest() throws Exception {
        Page<LeaveResponse> page = new PageImpl<>(List.of(sampleLeave()));
        when(leaveService.getPendingLeaves(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/leaves/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/leaves — should return 200 on valid leave request")
    void applyLeaveSuccessTest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto(
                1L, LeaveType.ANNUAL,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2),
                "Holiday");
        when(leaveService.applyLeave(any(), any())).thenReturn(sampleLeave());

        mockMvc.perform(post("/api/v1/leaves")
                        .with(authentication(mockAuth("emp@fintrack.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveType").value("ANNUAL"))
                .andExpect(jsonPath("$.totalDays").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/leaves — should return 400 when required fields missing")
    void applyLeaveValidationFailTest() throws Exception {
        String body = "{\"reason\":\"Holiday\"}";

        mockMvc.perform(post("/api/v1/leaves")
                        .with(authentication(mockAuth("emp@fintrack.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/leaves/{id}/approve — should return 200 with APPROVED status")
    void approveLeaveTest() throws Exception {
        LeaveResponse approved = new LeaveResponse(
                1L, 1L, "Noor Amin", LeaveType.ANNUAL,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2),
                2, "Holiday", LeaveStatus.APPROVED, null, LocalDateTime.now());
        when(leaveService.approveLeave(eq(1L), any())).thenReturn(approved);

        mockMvc.perform(put("/api/v1/leaves/1/approve")
                        .with(authentication(mockAuth("hr@fintrack.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PUT /api/v1/leaves/{id}/reject — should return 200 with REJECTED status and reason")
    void rejectLeaveTest() throws Exception {
        LeaveResponse rejected = new LeaveResponse(
                1L, 1L, "Noor Amin", LeaveType.ANNUAL,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2),
                2, "Holiday", LeaveStatus.REJECTED, "Insufficient documentation", LocalDateTime.now());
        when(leaveService.rejectLeave(eq(1L), any(), any())).thenReturn(rejected);

        mockMvc.perform(put("/api/v1/leaves/1/reject")
                        .with(authentication(mockAuth("hr@fintrack.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Insufficient documentation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Insufficient documentation"));
    }
}
