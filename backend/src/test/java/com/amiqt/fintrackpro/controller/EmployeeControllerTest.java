package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.model.dto.response.EmployeeResponse;
import com.amiqt.fintrackpro.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = EmployeeController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private com.amiqt.fintrackpro.security.JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private EmployeeResponse sampleEmployee() {
        return new EmployeeResponse(
                1L, "EMP001", "Noor Amin", "noor@fintrack.com",
                "012-3456789", "Engineering", "Senior Dev",
                BigDecimal.valueOf(8000), BigDecimal.valueOf(500), BigDecimal.valueOf(300),
                null, EmployeeStatus.ACTIVE, "SINGLE", 0, new BigDecimal("0.11")
        );
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} — should return 200 with employee data")
    void getEmployeeByIdSuccess() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleEmployee());

        mockMvc.perform(get("/api/v1/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Noor Amin"))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} — should return 404 when employee not found")
    void getEmployeeByIdNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/v1/employees/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} — should return 204 no content")
    void deleteEmployeeSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());
    }
}
