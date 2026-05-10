package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.service.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payslip")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;

    @GetMapping("/{payrollId}/pdf")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long payrollId) {
        byte[] pdfBytes = payslipService.generatePayslipPdf(payrollId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip-" + payrollId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
