package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.mapper.PayrollMapper;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.model.entity.Payroll;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import com.amiqt.fintrackpro.repository.PayrollRepository;
import com.amiqt.fintrackpro.repository.OutboxMessageRepository;
import com.amiqt.fintrackpro.service.payroll.EisCalculator;
import com.amiqt.fintrackpro.service.payroll.EpfCalculator;
import com.amiqt.fintrackpro.service.payroll.PcbCalculator;
import com.amiqt.fintrackpro.service.payroll.SocsoCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock private PayrollRepository payrollRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveRepository leaveRepository;
    @Mock private PayrollMapper payrollMapper;
    @Mock private NotificationService notificationService;
    @Mock private EpfCalculator epfCalculator;
    @Mock private SocsoCalculator socsoCalculator;
    @Mock private EisCalculator eisCalculator;
    @Mock private PcbCalculator pcbCalculator;
    @Mock private OutboxMessageRepository outboxMessageRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private PayrollService payrollService;

    private Employee employee;
    private Long employeeId;

    @BeforeEach
    void setUp() {
        employeeId = 1L;
        employee = new Employee();
        employee.setId(employeeId);
        employee.setEmployeeCode("EMP001");
        employee.setFullName("AmiQT Senior Dev");
        employee.setBasicSalary(new BigDecimal("5000.00"));
        employee.setHousingAllowance(new BigDecimal("500.00"));
        employee.setTransportAllowance(new BigDecimal("300.00"));
        employee.setStatus(EmployeeStatus.ACTIVE);

        when(epfCalculator.calculate(any(), any())).thenReturn(new BigDecimal("550.00"));
        when(epfCalculator.calculateEmployerContribution(any())).thenReturn(new BigDecimal("600.00"));
        when(socsoCalculator.calculate(any(), any())).thenReturn(new BigDecimal("19.75"));
        when(socsoCalculator.calculateEmployerContribution(any())).thenReturn(new BigDecimal("26.75"));
        when(eisCalculator.calculate(any(), any())).thenReturn(new BigDecimal("9.75"));
        when(eisCalculator.calculateEmployerContribution(any())).thenReturn(new BigDecimal("13.25"));
        when(pcbCalculator.calculate(any(), any())).thenReturn(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should calculate full salary correctly when no unpaid leaves")
    void calculateFullSalaryTest() {
        // Mocking
        when(payrollRepository.findByEmployeeIdAndMonthAndYear(any(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(leaveRepository.findByEmployeeIdAndStatus(any(), any())).thenReturn(Collections.emptyList());
        when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        Payroll result = payrollService.calculatePayroll(employee, 5, 2026);

        // Assertions
        assertNotNull(result);
        assertEquals(new BigDecimal("5800.00"), result.getGrossSalary());
        assertEquals(new BigDecimal("550.00"), result.getEpfEmployee());
        
        verify(payrollRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should deduct salary correctly for unpaid leaves")
    void calculateSalaryWithUnpaidLeavesTest() {
        // Setup a mock leave (2 days unpaid)
        LeaveRequest unpaidLeave = new LeaveRequest();
        unpaidLeave.setTotalDays(2);
        unpaidLeave.setLeaveType(LeaveType.UNPAID);
        unpaidLeave.setStartDate(java.time.LocalDate.of(2026, 5, 10));
        unpaidLeave.setEndDate(java.time.LocalDate.of(2026, 5, 11));

        when(payrollRepository.findByEmployeeIdAndMonthAndYear(any(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(leaveRepository.findByEmployeeIdAndStatus(any(), any()))
                .thenReturn(Collections.singletonList(unpaidLeave));
        when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        Payroll result = payrollService.calculatePayroll(employee, 5, 2026);

        // Deduction check
        assertTrue(result.getUnpaidLeaveDeduction().compareTo(BigDecimal.ZERO) > 0);
        
        verify(payrollRepository, times(1)).save(any());
    }
}
