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
import com.amiqt.fintrackpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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
        log.info("Applying {} leave for employee: {} ({} to {})",
                request.leaveType(), employee.getFullName(), request.startDate(), request.endDate());
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

        LeaveRequest savedLeave = leaveRepository.save(leaveRequest);

        try {
            List<User> hrAndAdmins = userRepository.findByRoleIn(List.of(
                    com.amiqt.fintrackpro.enums.Role.ROLE_ADMIN,
                    com.amiqt.fintrackpro.enums.Role.ROLE_HR
            ));
            String title = "New Leave Request: " + employee.getFullName();
            String message = employee.getFullName() + " has requested " + request.leaveType() + " Leave from " + request.startDate() + " to " + request.endDate() + ".";
            for (User recipient : hrAndAdmins) {
                notificationService.createNotification(recipient, title, message);
            }
        } catch (Exception e) {
            log.error("Failed to send leave notifications for employee {}: {}", employee.getFullName(), e.getMessage());
        }

        log.info("Leave request saved — id: {}, days: {}", savedLeave.getId(), totalDays);
        return mapToResponse(savedLeave);
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

        LeaveRequest savedLeave = leaveRepository.save(leaveRequest);
        log.info("Leave id {} approved by: {}", id, reviewer.getEmail());

        try {
            String title = "Leave Request Approved";
            String message = "Your " + savedLeave.getLeaveType() + " Leave request from " + savedLeave.getStartDate() + " to " + savedLeave.getEndDate() + " has been approved.";
            notificationService.createNotification(savedLeave.getEmployee().getUser(), title, message);
        } catch (Exception e) {
            log.error("Failed to send leave approval notification for leave id {}: {}", id, e.getMessage());
        }

        return mapToResponse(savedLeave);
    }

    @Transactional
    public LeaveResponse rejectLeave(Long id, String reason, User reviewer) {
        LeaveRequest leaveRequest = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason(reason);
        leaveRequest.setApprovedBy(reviewer);
        leaveRequest.setReviewedAt(LocalDateTime.now());

        LeaveRequest savedLeave = leaveRepository.save(leaveRequest);
        log.info("Leave id {} rejected by: {} — reason: {}", id, reviewer.getEmail(), reason);

        try {
            String title = "Leave Request Rejected";
            String message = "Your " + savedLeave.getLeaveType() + " Leave request from " + savedLeave.getStartDate() + " to " + savedLeave.getEndDate() + " has been rejected. Reason: " + reason;
            notificationService.createNotification(savedLeave.getEmployee().getUser(), title, message);
        } catch (Exception e) {
            log.error("Failed to send leave rejection notification for leave id {}: {}", id, e.getMessage());
        }

        return mapToResponse(savedLeave);
    }

    public List<LeaveResponse> getMyLeaves(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));

        return leaveRepository.findByEmployeeId(employee.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<LeaveResponse> getPendingLeaves(Pageable pageable) {
        return leaveRepository.findByStatus(LeaveStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    public Page<LeaveResponse> getAllLeaves(Pageable pageable) {
        return leaveRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public byte[] exportLeavesToExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Leave Requests");

            // Header style — dark purple
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            ((XSSFCellStyle) headerStyle).setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 88, (byte) 28, (byte) 135}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Status styles
            CellStyle approvedStyle = workbook.createCellStyle();
            XSSFFont approvedFont = workbook.createFont();
            approvedFont.setColor(new XSSFColor(new byte[]{(byte) 21, (byte) 128, (byte) 61}, null));
            approvedFont.setBold(true);
            approvedStyle.setFont(approvedFont);

            CellStyle rejectedStyle = workbook.createCellStyle();
            XSSFFont rejectedFont = workbook.createFont();
            rejectedFont.setColor(new XSSFColor(new byte[]{(byte) 185, (byte) 28, (byte) 28}, null));
            rejectedFont.setBold(true);
            rejectedStyle.setFont(rejectedFont);

            CellStyle pendingStyle = workbook.createCellStyle();
            XSSFFont pendingFont = workbook.createFont();
            pendingFont.setColor(new XSSFColor(new byte[]{(byte) 180, (byte) 83, (byte) 9}, null));
            pendingFont.setBold(true);
            pendingStyle.setFont(pendingFont);

            // Header row
            String[] headers = {"No.", "Employee Name", "Leave Type", "Start Date", "End Date", "Days", "Status", "Reason"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            List<LeaveRequest> leaves = leaveRepository.findAll();
            int rowNum = 1;
            for (LeaveRequest l : leaves) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(l.getEmployee() != null && l.getEmployee().getFullName() != null ? l.getEmployee().getFullName() : "");
                row.createCell(2).setCellValue(l.getLeaveType() != null ? l.getLeaveType().name() : "");
                row.createCell(3).setCellValue(l.getStartDate() != null ? l.getStartDate().toString() : "");
                row.createCell(4).setCellValue(l.getEndDate() != null ? l.getEndDate().toString() : "");
                row.createCell(5).setCellValue(l.getTotalDays() != null ? l.getTotalDays() : 0);

                Cell statusCell = row.createCell(6);
                String status = l.getStatus() != null ? l.getStatus().name() : "";
                statusCell.setCellValue(status);
                if ("APPROVED".equals(status)) statusCell.setCellStyle(approvedStyle);
                else if ("REJECTED".equals(status)) statusCell.setCellStyle(rejectedStyle);
                else statusCell.setCellStyle(pendingStyle);

                row.createCell(7).setCellValue(l.getReason() != null ? l.getReason() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Leave Excel: " + e.getMessage(), e);
        }
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
