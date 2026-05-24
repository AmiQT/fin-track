package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.PayrollRequest;
import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.AuditLogService;
import com.amiqt.fintrackpro.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Management", description = "Endpoints for processing, viewing, and exporting payroll data")
public class PayrollController {

    private final PayrollService payrollService;
    private final AuditLogService auditLogService;

    @PostMapping("/process")
    @Operation(summary = "Process monthly payroll", description = "Calculates payroll for all active employees for the specified month and year")
    @ApiResponse(responseCode = "200", description = "Payroll processed successfully")
    public ResponseEntity<List<PayrollResponse>> processPayroll(
            @Valid @RequestBody PayrollRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest
    ) {
        log.info("Processing payroll for {}/{}", request.month(), request.year());
        List<PayrollResponse> result = payrollService.processPayroll(request);
        log.info("Payroll processed for {} employees — {}/{}", result.size(), request.month(), request.year());
        auditLogService.log(user.getEmail(), "PROCESS_PAYROLL", "Payroll",
                request.month() + "/" + request.year(),
                result.size() + " employees processed",
                httpRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user payroll history")
    public ResponseEntity<List<PayrollResponse>> getMyPayroll(@AuthenticationPrincipal User user) {
        log.debug("Payroll history request for: {}", user.getEmail());
        return ResponseEntity.ok(payrollService.getMyPayroll(user.getEmail()));
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get payroll history for a specific employee", description = "Requires ADMIN role")
    public ResponseEntity<Page<PayrollResponse>> getPayrollHistory(
            @PathVariable Long employeeId,
            @PageableDefault(size = 12, sort = "year,month") Pageable pageable) {
        log.debug("Payroll history for employee id: {}", employeeId);
        return ResponseEntity.ok(payrollService.getPayrollHistory(employeeId, pageable));
    }

    @GetMapping("/export/{month}/{year}")
    @Operation(summary = "Export payroll report to CSV")
    public ResponseEntity<byte[]> exportPayrollToCsv(@PathVariable Integer month, @PathVariable Integer year) {
        log.info("Exporting payroll CSV for {}/{}", month, year);
        String csv = payrollService.exportPayrollToCsv(month, year);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=payroll_" + month + "_" + year + ".csv")
                .body(csv.getBytes());
    }

    @GetMapping("/export/excel/{month}/{year}")
    @Operation(summary = "Export payroll report to Excel", description = "Generates a detailed Excel report with statutory contributions (EPF, SOCSO, EIS)")
    public ResponseEntity<byte[]> exportPayrollToExcel(@PathVariable Integer month, @PathVariable Integer year) {
        log.info("Exporting payroll Excel for {}/{}", month, year);
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=payroll_" + month + "_" + year + ".xlsx")
                .body(payrollService.exportPayrollToExcel(month, year));
    }

    @GetMapping("/summary/{month}/{year}")
    @Operation(summary = "Get monthly payroll summary")
    public ResponseEntity<List<PayrollResponse>> getMonthlySummary(
            @PathVariable Integer month,
            @PathVariable Integer year
    ) {
        return ResponseEntity.ok(payrollService.getMonthlySummary(month, year));
    }
}
