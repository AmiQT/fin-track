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
import com.amiqt.fintrackpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRepository leaveRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private User admin;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("Noor Amin");
        employee.setAnnualLeaveEntitlement(14);
        employee.setSickLeaveEntitlement(14);

        admin = new User();
        admin.setEmail("admin@fintrack.com");
    }

    @Test
    @DisplayName("Should apply UNPAID leave and calculate 3 working days (Mon-Wed)")
    void applyUnpaidLeaveTest() {
        LeaveRequestDto requestDto = new LeaveRequestDto(
                1L,
                LeaveType.UNPAID,
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 5, 6),
                "Urgent personal matter"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any())).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });
        when(userRepository.findByRoleIn(any())).thenReturn(Collections.emptyList());

        LeaveResponse response = leaveService.applyLeave(requestDto);

        assertNotNull(response);
        assertEquals(3, response.totalDays());
        assertEquals(LeaveStatus.PENDING, response.status());
        verify(leaveRepository).save(any());
    }

    @Test
    @DisplayName("Should apply ANNUAL leave when sufficient balance")
    void applyAnnualLeaveWithSufficientBalanceTest() {
        LeaveRequestDto requestDto = new LeaveRequestDto(
                1L,
                LeaveType.ANNUAL,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                "Holiday"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRepository.sumTakenDaysByEmployeeAndTypeAndYear(any(), any(), anyInt())).thenReturn(0);
        when(leaveRepository.save(any())).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });
        when(userRepository.findByRoleIn(any())).thenReturn(Collections.emptyList());

        LeaveResponse response = leaveService.applyLeave(requestDto);

        assertNotNull(response);
        assertEquals(2, response.totalDays());
        assertEquals(LeaveStatus.PENDING, response.status());
    }

    @Test
    @DisplayName("Should throw exception when ANNUAL leave balance insufficient")
    void applyAnnualLeaveInsufficientBalanceTest() {
        LeaveRequestDto requestDto = new LeaveRequestDto(
                1L,
                LeaveType.ANNUAL,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 15),
                "Long holiday"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        // 13 days already taken, only 1 remaining — request is for 11 working days
        when(leaveRepository.sumTakenDaysByEmployeeAndTypeAndYear(any(), eq(LeaveType.ANNUAL), anyInt())).thenReturn(13);
        when(leaveRepository.sumTakenDaysByEmployeeAndTypeAndYear(any(), eq(LeaveType.SICK), anyInt())).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(requestDto));
    }

    @Test
    @DisplayName("Should approve leave and set status to APPROVED")
    void approveLeaveTest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(100L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(LeaveType.ANNUAL);
        leaveRequest.setStartDate(LocalDate.of(2026, 6, 1));
        leaveRequest.setEndDate(LocalDate.of(2026, 6, 2));
        leaveRequest.setStatus(LeaveStatus.PENDING);
        employee.setUser(admin);

        when(leaveRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRepository.save(any())).thenReturn(leaveRequest);

        LeaveResponse response = leaveService.approveLeave(100L, admin);

        assertEquals(LeaveStatus.APPROVED, response.status());
        verify(leaveRepository).save(leaveRequest);
    }

    @Test
    @DisplayName("Should reject leave and set status to REJECTED with reason")
    void rejectLeaveTest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(200L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(LeaveType.SICK);
        leaveRequest.setStartDate(LocalDate.of(2026, 7, 1));
        leaveRequest.setEndDate(LocalDate.of(2026, 7, 1));
        leaveRequest.setStatus(LeaveStatus.PENDING);
        employee.setUser(admin);

        when(leaveRepository.findById(200L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRepository.save(any())).thenReturn(leaveRequest);

        LeaveResponse response = leaveService.rejectLeave(200L, "Insufficient documentation", admin);

        assertEquals(LeaveStatus.REJECTED, response.status());
        assertEquals("Insufficient documentation", response.rejectionReason());
        verify(leaveRepository).save(leaveRequest);
    }

    @Test
    @DisplayName("Should return empty list for employee with no leaves")
    void getMyLeavesEmptyTest() {
        when(employeeRepository.findByUserEmail("noor@fintrack.com")).thenReturn(Optional.of(employee));
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());

        List<LeaveResponse> result = leaveService.getMyLeaves("noor@fintrack.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
