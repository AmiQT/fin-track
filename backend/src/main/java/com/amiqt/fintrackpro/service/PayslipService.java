package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.model.entity.Payroll;
import com.amiqt.fintrackpro.repository.PayrollRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PayslipService {

    private final PayrollRepository payrollRepository;

    private String fmt(BigDecimal value) {
        return value != null ? value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
    }

    @Transactional(readOnly = true)
    public byte[] generatePayslipPdf(Long payrollId) {
        System.out.println(">>> Generating PDF for Payroll ID: " + payrollId);
        
        try {
            Payroll payroll = payrollRepository.findById(payrollId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found for ID: " + payrollId));

            System.out.println(">>> Found payroll for employee: " + payroll.getEmployee().getFullName());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            document.add(new Paragraph("FinTrack Pro")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("OFFICIAL PAYSLIP")
                    .setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Employee Info
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            infoTable.addCell(new Cell().add(new Paragraph("Employee Name:")).setBold());
            infoTable.addCell(new Cell().add(new Paragraph(payroll.getEmployee() != null ? payroll.getEmployee().getFullName() : "N/A")));
            infoTable.addCell(new Cell().add(new Paragraph("Employee Code:")).setBold());
            infoTable.addCell(new Cell().add(new Paragraph(payroll.getEmployee() != null ? payroll.getEmployee().getEmployeeCode() : "N/A")));
            infoTable.addCell(new Cell().add(new Paragraph("Pay Period:")).setBold());
            infoTable.addCell(new Cell().add(new Paragraph(payroll.getMonth() + "/" + payroll.getYear())));
            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Earnings Table
            document.add(new Paragraph("Earnings").setBold().setUnderline());
            Table earningsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            earningsTable.addCell(new Cell().add(new Paragraph("Basic Salary")));
            earningsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getBasicSalary()))).setTextAlignment(TextAlignment.RIGHT));
            earningsTable.addCell(new Cell().add(new Paragraph("Housing Allowance")));
            earningsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getHousingAllowance()))).setTextAlignment(TextAlignment.RIGHT));
            earningsTable.addCell(new Cell().add(new Paragraph("Transport Allowance")));
            earningsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getTransportAllowance()))).setTextAlignment(TextAlignment.RIGHT));
            earningsTable.addCell(new Cell().add(new Paragraph("Gross Salary")).setBold());
            earningsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getGrossSalary()))).setBold().setTextAlignment(TextAlignment.RIGHT));
            document.add(earningsTable);
            document.add(new Paragraph("\n"));

            // Deductions Table
            document.add(new Paragraph("Deductions").setBold().setUnderline());
            Table deductionsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            deductionsTable.addCell(new Cell().add(new Paragraph("EPF - Employee (11%)")));
            deductionsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getEpfEmployee()))).setTextAlignment(TextAlignment.RIGHT));
            deductionsTable.addCell(new Cell().add(new Paragraph("SOCSO - Employee (0.5%)")));
            deductionsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getSocsoEmployee()))).setTextAlignment(TextAlignment.RIGHT));
            deductionsTable.addCell(new Cell().add(new Paragraph("Income Tax (PCB)")));
            deductionsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getIncomeTax()))).setTextAlignment(TextAlignment.RIGHT));
            deductionsTable.addCell(new Cell().add(new Paragraph("Unpaid Leave Deduction")));
            deductionsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getUnpaidLeaveDeduction()))).setTextAlignment(TextAlignment.RIGHT));
            deductionsTable.addCell(new Cell().add(new Paragraph("Total Deductions")).setBold());
            deductionsTable.addCell(new Cell().add(new Paragraph("RM " + fmt(payroll.getTotalDeductions()))).setBold().setTextAlignment(TextAlignment.RIGHT));
            document.add(deductionsTable);
            document.add(new Paragraph("\n"));

            // Net Salary Summary
            document.add(new Paragraph("NET SALARY: RM " + fmt(payroll.getNetSalary()))
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("\n\nGenerated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(8).setTextAlignment(TextAlignment.CENTER));

            document.close();
            System.out.println(">>> PDF Generation Success!");
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("!!! PDF GENERATION ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}
