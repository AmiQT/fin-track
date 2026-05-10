package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.PayrollRequest;
import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/process")
    public ResponseEntity<List<PayrollResponse>> processPayroll(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.ok(payrollService.processPayroll(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PayrollResponse>> getMyPayroll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(payrollService.getMyPayroll(user.getEmail()));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<PayrollResponse>> getPayrollHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getPayrollHistory(employeeId));
    }

    @GetMapping("/export/{month}/{year}")
    public ResponseEntity<byte[]> exportPayrollToCsv(@PathVariable Integer month, @PathVariable Integer year) {
        String csv = payrollService.exportPayrollToCsv(month, year);
        byte[] csvBytes = csv.getBytes();

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=payroll_" + month + "_" + year + ".csv")
                .body(csvBytes);
    }

    @GetMapping("/summary/{month}/{year}")
    public ResponseEntity<List<PayrollResponse>> getMonthlySummary(
            @PathVariable Integer month,
            @PathVariable Integer year
    ) {
        return ResponseEntity.ok(payrollService.getMonthlySummary(month, year));
    }
}