package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.PayrollRequest;
import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.AuditLogService;
import com.amiqt.fintrackpro.service.PayrollService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PayrollController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PayrollService payrollService;

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

    private PayrollResponse samplePayroll() {
        return new PayrollResponse(
                1L, 1L, "Noor Amin",
                5, 2026,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5800),
                BigDecimal.valueOf(550), BigDecimal.valueOf(600),
                BigDecimal.valueOf(19.75), BigDecimal.valueOf(26.75),
                BigDecimal.valueOf(9.75), BigDecimal.valueOf(13.25),
                BigDecimal.valueOf(150), BigDecimal.ZERO,
                BigDecimal.valueOf(729.50), BigDecimal.valueOf(4870.50),
                null);
    }

    @Test
    @DisplayName("POST /api/v1/payroll/process — should return 200 with processed payroll list")
    void processPayrollSuccessTest() throws Exception {
        PayrollRequest request = new PayrollRequest(5, 2026);
        when(payrollService.processPayroll(any())).thenReturn(List.of(samplePayroll()));

        mockMvc.perform(post("/api/v1/payroll/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("Noor Amin"))
                .andExpect(jsonPath("$[0].month").value(5))
                .andExpect(jsonPath("$[0].year").value(2026));
    }

    @Test
    @DisplayName("POST /api/v1/payroll/process — should return 400 when month or year missing")
    void processPayrollValidationFailTest() throws Exception {
        String body = "{\"month\":13}";

        mockMvc.perform(post("/api/v1/payroll/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/payroll/{employeeId} — should return 200 with paginated payroll history")
    void getPayrollHistoryTest() throws Exception {
        Page<PayrollResponse> page = new PageImpl<>(List.of(samplePayroll()));
        when(payrollService.getPayrollHistory(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/payroll/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].netSalary").value(4870.50));
    }

    @Test
    @DisplayName("GET /api/v1/payroll/my — should return 200 with current user payroll")
    void getMyPayrollTest() throws Exception {
        when(payrollService.getMyPayroll(any())).thenReturn(List.of(samplePayroll()));

        mockMvc.perform(get("/api/v1/payroll/my")
                        .with(authentication(mockAuth("noor@fintrack.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("Noor Amin"));
    }

    @Test
    @DisplayName("GET /api/v1/payroll/summary/{month}/{year} — should return 200 with monthly summary")
    void getMonthlySummaryTest() throws Exception {
        when(payrollService.getMonthlySummary(5, 2026)).thenReturn(List.of(samplePayroll()));

        mockMvc.perform(get("/api/v1/payroll/summary/5/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(5))
                .andExpect(jsonPath("$[0].year").value(2026));
    }
}
