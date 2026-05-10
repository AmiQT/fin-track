package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.model.dto.request.LeaveRequestDto;
import com.amiqt.fintrackpro.model.dto.response.LeaveResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    @Test
    @DisplayName("Should apply leave correctly and calculate working days")
    void applyLeaveTest() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setFullName("Noor Amin");

        // Request: 5 May (Mon) to 7 May (Wed) 2026 -> 3 working days
        LeaveRequestDto requestDto = new LeaveRequestDto(
                employeeId, 
                LeaveType.ANNUAL, 
                LocalDate.of(2026, 5, 4), 
                LocalDate.of(2026, 5, 6), 
                "Holiday"
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any())).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        LeaveResponse response = leaveService.applyLeave(requestDto);

        assertNotNull(response);
        assertEquals(3, response.totalDays());
        assertEquals(LeaveStatus.PENDING, response.status());
        verify(leaveRepository).save(any());
    }

    @Test
    @DisplayName("Should approve leave request")
    void approveLeaveTest() {
        Long leaveId = 100L;
        User admin = new User();
        admin.setEmail("admin@fintrack.com");

        Employee employee = new Employee();
        employee.setFullName("Noor Amin");

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(leaveId);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRepository.save(any())).thenReturn(leaveRequest);

        LeaveResponse response = leaveService.approveLeave(leaveId, admin);

        assertEquals(LeaveStatus.APPROVED, response.status());
        verify(leaveRepository).save(leaveRequest);
    }
}
