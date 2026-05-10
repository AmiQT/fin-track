package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.model.dto.request.LeaveRequestDto;
import com.amiqt.fintrackpro.model.dto.response.LeaveBalanceResponse;
import com.amiqt.fintrackpro.model.dto.response.LeaveResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveResponse applyLeave(LeaveRequestDto request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return applyLeaveInternal(request, employee);
    }

    @Transactional
    public LeaveResponse applyLeave(LeaveRequestDto request, String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));
        return applyLeaveInternal(request, employee);
    }

    private LeaveResponse applyLeaveInternal(LeaveRequestDto request, Employee employee) {
        int totalDays = calculateLeaveDays(request.startDate(), request.endDate());

        if (request.leaveType() == com.amiqt.fintrackpro.enums.LeaveType.ANNUAL || request.leaveType() == com.amiqt.fintrackpro.enums.LeaveType.SICK) {
            LeaveBalanceResponse balance = getLeaveBalanceInternal(employee);
            if (request.leaveType() == com.amiqt.fintrackpro.enums.LeaveType.ANNUAL && totalDays > balance.annualBalance()) {
                throw new IllegalArgumentException("Insufficient Annual Leave balance. Remaining: " + balance.annualBalance() + " days.");
            }
            if (request.leaveType() == com.amiqt.fintrackpro.enums.LeaveType.SICK && totalDays > balance.sickBalance()) {
                throw new IllegalArgumentException("Insufficient Sick Leave balance. Remaining: " + balance.sickBalance() + " days.");
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalDays(totalDays)
                .reason(request.reason())
                .status(LeaveStatus.PENDING)
                .build();

        return mapToResponse(leaveRepository.save(leaveRequest));
    }

    public com.amiqt.fintrackpro.model.dto.response.LeaveBalanceResponse getLeaveBalance(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));
        return getLeaveBalanceInternal(employee);
    }

    private com.amiqt.fintrackpro.model.dto.response.LeaveBalanceResponse getLeaveBalanceInternal(Employee employee) {
        int currentYear = LocalDate.now().getYear();
        
        Integer annualTaken = leaveRepository.sumTakenDaysByEmployeeAndTypeAndYear(
                employee.getId(), com.amiqt.fintrackpro.enums.LeaveType.ANNUAL, currentYear);
        Integer sickTaken = leaveRepository.sumTakenDaysByEmployeeAndTypeAndYear(
                employee.getId(), com.amiqt.fintrackpro.enums.LeaveType.SICK, currentYear);

        int annualEntitlement = employee.getAnnualLeaveEntitlement() != null ? employee.getAnnualLeaveEntitlement() : 14;
        int sickEntitlement = employee.getSickLeaveEntitlement() != null ? employee.getSickLeaveEntitlement() : 14;

        return new com.amiqt.fintrackpro.model.dto.response.LeaveBalanceResponse(
                annualEntitlement,
                annualTaken,
                Math.max(0, annualEntitlement - annualTaken),
                sickEntitlement,
                sickTaken,
                Math.max(0, sickEntitlement - sickTaken)
        );
    }

    @Transactional
    public LeaveResponse approveLeave(Long id, User reviewer) {
        LeaveRequest leaveRequest = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(reviewer);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        
        return mapToResponse(leaveRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveResponse rejectLeave(Long id, String reason, User reviewer) {
        LeaveRequest leaveRequest = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason(reason);
        leaveRequest.setApprovedBy(reviewer);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        
        return mapToResponse(leaveRepository.save(leaveRequest));
    }

    public List<LeaveResponse> getMyLeaves(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));

        return leaveRepository.findByEmployeeId(employee.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveResponse> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private int calculateLeaveDays(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    private LeaveResponse mapToResponse(LeaveRequest l) {
        return new LeaveResponse(
                l.getId(),
                l.getEmployee().getId(),
                l.getEmployee().getFullName(),
                l.getLeaveType(),
                l.getStartDate(),
                l.getEndDate(),
                l.getTotalDays(),
                l.getReason(),
                l.getStatus(),
                l.getRejectionReason(),
                l.getAppliedAt()
        );
    }
}
