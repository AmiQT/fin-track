package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.service.PayslipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payslip")
@RequiredArgsConstructor
@Tag(name = "Payslip", description = "Endpoints for downloading payslips as PDF")
public class PayslipController {

    private final PayslipService payslipService;

    @GetMapping("/{payrollId}/pdf")
    @Operation(summary = "Download payslip PDF for a given payroll record")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long payrollId) {
        log.info("Payslip PDF requested for payroll id: {}", payrollId);
        byte[] pdfBytes = payslipService.generatePayslipPdf(payrollId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip-" + payrollId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
