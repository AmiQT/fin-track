package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.exception.PayrollAlreadyProcessedException;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.mapper.PayrollMapper;
import com.amiqt.fintrackpro.model.dto.request.PayrollRequest;
import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.Payroll;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.model.entity.OutboxMessage;
import com.amiqt.fintrackpro.model.event.ProcessPayrollCommand;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.PayrollRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import com.amiqt.fintrackpro.repository.OutboxMessageRepository;
import com.amiqt.fintrackpro.service.payroll.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollMapper payrollMapper;
    private final NotificationService notificationService;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    // Payroll Calculators (Strategy Pattern)
    private final EpfCalculator epfCalculator;
    private final SocsoCalculator socsoCalculator;
    private final EisCalculator eisCalculator;
    private final PcbCalculator pcbCalculator;

    @Transactional
    @CacheEvict(value = "dashboard-summary", allEntries = true)
    public List<PayrollResponse> processPayroll(PayrollRequest request) {
        log.info("Queuing asynchronous payroll processing for {}/{}", request.month(), request.year());
        
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus().name().equals("ACTIVE"))
                .toList();

        UUID transactionId = UUID.randomUUID();

        List<PayrollResponse> results = activeEmployees.stream()
                .map(employee -> {
                    // Check if payroll already exists to avoid redundant queue events
                    payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), request.month(), request.year())
                            .ifPresent(p -> {
                                throw new PayrollAlreadyProcessedException(
                                        "Payroll already processed for employee " + employee.getEmployeeCode() + " for " + request.month() + "/" + request.year()
                                );
                            });

                    // Construct Async Command DTO
                    ProcessPayrollCommand command = new ProcessPayrollCommand(
                            employee.getId(),
                            request.month(),
                            request.year(),
                            transactionId
                    );
                    
                    try {
                        String payloadJson = objectMapper.writeValueAsString(command);
                        OutboxMessage outboxMessage = OutboxMessage.builder()
                                .id(UUID.randomUUID())
                                .topic("payroll-commands")
                                .payload(payloadJson)
                                .status("PENDING")
                                .retryCount(0)
                                .build();
                        
                        log.info("Persisting ProcessPayrollCommand to outbox for employee id: {} (TxID: {})", employee.getId(), transactionId);
                        outboxMessageRepository.save(outboxMessage);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to serialize ProcessPayrollCommand for employee id: {}: {}", employee.getId(), e.getMessage());
                        throw new IllegalArgumentException("Failed to serialize payroll event", e);
                    }

                    // Return simulated response indicating processing state
                    return new PayrollResponse(
                            null,
                            employee.getId(),
                            employee.getFullName(),
                            request.month(),
                            request.year(),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            LocalDateTime.now()
                    );
                })
                .collect(Collectors.toList());

        log.info("Queued {} payroll command events successfully in Apache Kafka for {}/{}", results.size(), request.month(), request.year());
        return results;
    }

    public Payroll calculatePayroll(Employee employee, Integer month, Integer year) {
        payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), month, year)
                .ifPresent(p -> {
                    throw new PayrollAlreadyProcessedException(
                            "Payroll already processed for employee " + employee.getEmployeeCode() + " for " + month + "/" + year
                    );
                });

        BigDecimal basic = employee.getBasicSalary() != null ? employee.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal housing = employee.getHousingAllowance() != null ? employee.getHousingAllowance() : BigDecimal.ZERO;
        BigDecimal transport = employee.getTransportAllowance() != null ? employee.getTransportAllowance() : BigDecimal.ZERO;

        BigDecimal gross = basic.add(housing).add(transport);

        long unpaidDays = calculateUnpaidLeaveDays(employee.getId(), month, year);
        int workingDays = calculateWorkingDays(month, year);
        BigDecimal unpaidDeduction = BigDecimal.ZERO;
        if (unpaidDays > 0) {
            unpaidDeduction = basic.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(unpaidDays));
        }

        BigDecimal adjustedGross = gross.subtract(unpaidDeduction);
        
        // EPF Calculation using Strategy
        BigDecimal epfEmployee = epfCalculator.calculate(basic, employee);
        BigDecimal epfEmployer = epfCalculator.calculateEmployerContribution(basic);
        
        // SOCSO & EIS Calculation using Strategy
        BigDecimal socsoEmployee = socsoCalculator.calculate(basic, employee);
        BigDecimal socsoEmployer = socsoCalculator.calculateEmployerContribution(basic);
        BigDecimal eisEmployee = eisCalculator.calculate(basic, employee);
        BigDecimal eisEmployer = eisCalculator.calculateEmployerContribution(basic);

        // PCB (Income Tax) Calculation using Strategy
        BigDecimal incomeTax = pcbCalculator.calculate(adjustedGross, employee);

        BigDecimal totalDeductions = epfEmployee.add(socsoEmployee).add(eisEmployee).add(incomeTax).add(unpaidDeduction);
        BigDecimal netSalary = adjustedGross.subtract(epfEmployee).subtract(socsoEmployee).subtract(eisEmployee).subtract(incomeTax);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(month)
                .year(year)
                .basicSalary(basic)
                .housingAllowance(housing)
                .transportAllowance(transport)
                .grossSalary(gross)
                .epfEmployee(epfEmployee)
                .epfEmployer(epfEmployer)
                .socsoEmployee(socsoEmployee)
                .socsoEmployer(socsoEmployer)
                .eisEmployee(eisEmployee)
                .eisEmployer(eisEmployer)
                .incomeTax(incomeTax)
                .unpaidLeaveDeduction(unpaidDeduction)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .build();

        Payroll savedPayroll = payrollRepository.save(payroll);
        log.info("Payroll saved for employee {} — net salary: RM {}", employee.getEmployeeCode(), netSalary);

        try {
            String monthLabel = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            String title = "Payslip Ready: " + monthLabel + " " + year;
            String message = "Your payslip for " + monthLabel + " " + year + " has been processed. Net Salary: RM " + savedPayroll.getNetSalary() + ".";
            notificationService.createNotification(employee.getUser(), title, message);
        } catch (Exception e) {
            log.error("Failed to send payroll notification for employee {}: {}", employee.getEmployeeCode(), e.getMessage());
        }

        return savedPayroll;
    }

    private int calculateWorkingDays(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int workingDays = 0;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays;
    }

    private long calculateUnpaidLeaveDays(Long employeeId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

        return leaveRepository.findByEmployeeIdAndStatus(employeeId, LeaveStatus.APPROVED)
                .stream()
                .filter(l -> l.getLeaveType() == LeaveType.UNPAID)
                .filter(l -> !l.getStartDate().isAfter(endOfMonth) && !l.getEndDate().isBefore(startOfMonth))
                .mapToLong(LeaveRequest::getTotalDays)
                .sum();
    }

    public Page<PayrollResponse> getPayrollHistory(Long employeeId, Pageable pageable) {
        return payrollRepository.findByEmployeeId(employeeId, pageable)
                .map(payrollMapper::toResponse);
    }

    public List<PayrollResponse> getMyPayroll(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));
        return payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employee.getId()).stream()
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "payroll-summary", key = "#month + '-' + #year")
    public List<PayrollResponse> getMonthlySummary(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year).stream()
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());
    }

    public String exportPayrollToCsv(Integer month, Integer year) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee Name,Month,Year,Basic Salary,Housing,Transport,Gross Salary,EPF,SOCSO,Tax,Net Salary\n");

        payrollRepository.findByMonthAndYear(month, year).forEach(p -> {
            csv.append(String.format("%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                    p.getEmployee().getFullName(),
                    p.getMonth(),
                    p.getYear(),
                    p.getBasicSalary(),
                    p.getHousingAllowance(),
                    p.getTransportAllowance(),
                    p.getGrossSalary(),
                    p.getEpfEmployee(),
                    p.getSocsoEmployee(),
                    p.getIncomeTax(),
                    p.getNetSalary()));
        });

        return csv.toString();
    }

    public byte[] exportPayrollToExcel(Integer month, Integer year) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            Sheet sheet = workbook.createSheet("Payroll " + monthName + " " + year);

            // Header style — dark green
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            ((XSSFCellStyle) headerStyle).setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 21, (byte) 128, (byte) 61}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            DataFormat dataFormat = workbook.createDataFormat();
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            // Deduction style — red tint
            CellStyle deductStyle = workbook.createCellStyle();
            deductStyle.cloneStyleFrom(moneyStyle);
            XSSFFont deductFont = workbook.createFont();
            deductFont.setColor(new XSSFColor(new byte[]{(byte) 185, (byte) 28, (byte) 28}, null));
            deductStyle.setFont(deductFont);

            // Summary style
            CellStyle summaryStyle = workbook.createCellStyle();
            XSSFFont summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            ((XSSFCellStyle) summaryStyle).setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 254, (byte) 240, (byte) 138}, null));
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle summaryMoneyStyle = workbook.createCellStyle();
            summaryMoneyStyle.cloneStyleFrom(summaryStyle);
            summaryMoneyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PAYROLL REPORT — " + monthName.toUpperCase() + " " + year);
            CellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            titleRow.setHeightInPoints(28);

            // Header row
            String[] headers = {"No.", "Employee Name", "Basic (RM)", "Housing (RM)", "Transport (RM)",
                    "Gross (RM)", "EPF (RM)", "SOCSO (RM)", "EIS (RM)", "Tax (RM)", "Total Deductions (RM)", "Net Salary (RM)"};
            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            List<Payroll> payrolls = payrollRepository.findByMonthAndYear(month, year);
            int rowNum = 3;
            BigDecimal totalNet = BigDecimal.ZERO;
            BigDecimal totalGross = BigDecimal.ZERO;

            for (Payroll p : payrolls) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 3);
                row.createCell(1).setCellValue(p.getEmployee().getFullName() != null ? p.getEmployee().getFullName() : "");

                Cell basicCell = row.createCell(2);
                basicCell.setCellValue(p.getBasicSalary() != null ? p.getBasicSalary().doubleValue() : 0);
                basicCell.setCellStyle(moneyStyle);

                Cell housingCell = row.createCell(3);
                housingCell.setCellValue(p.getHousingAllowance() != null ? p.getHousingAllowance().doubleValue() : 0);
                housingCell.setCellStyle(moneyStyle);

                Cell transportCell = row.createCell(4);
                transportCell.setCellValue(p.getTransportAllowance() != null ? p.getTransportAllowance().doubleValue() : 0);
                transportCell.setCellStyle(moneyStyle);

                Cell grossCell = row.createCell(5);
                grossCell.setCellValue(p.getGrossSalary() != null ? p.getGrossSalary().doubleValue() : 0);
                grossCell.setCellStyle(moneyStyle);

                Cell epfCell = row.createCell(6);
                epfCell.setCellValue(p.getEpfEmployee() != null ? p.getEpfEmployee().doubleValue() : 0);
                epfCell.setCellStyle(deductStyle);

                Cell socsoCell = row.createCell(7);
                socsoCell.setCellValue(p.getSocsoEmployee() != null ? p.getSocsoEmployee().doubleValue() : 0);
                socsoCell.setCellStyle(deductStyle);

                Cell eisCell = row.createCell(8);
                eisCell.setCellValue(p.getEisEmployee() != null ? p.getEisEmployee().doubleValue() : 0);
                eisCell.setCellStyle(deductStyle);

                Cell taxCell = row.createCell(9);
                taxCell.setCellValue(p.getIncomeTax() != null ? p.getIncomeTax().doubleValue() : 0);
                taxCell.setCellStyle(deductStyle);

                Cell totalDeductCell = row.createCell(10);
                totalDeductCell.setCellValue(p.getTotalDeductions() != null ? p.getTotalDeductions().doubleValue() : 0);
                totalDeductCell.setCellStyle(deductStyle);

                Cell netCell = row.createCell(11);
                netCell.setCellValue(p.getNetSalary() != null ? p.getNetSalary().doubleValue() : 0);
                netCell.setCellStyle(moneyStyle);

                if (p.getNetSalary() != null) totalNet = totalNet.add(p.getNetSalary());
                if (p.getGrossSalary() != null) totalGross = totalGross.add(p.getGrossSalary());
            }

            // Summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            Cell summaryLabel = summaryRow.createCell(1);
            summaryLabel.setCellValue("TOTAL (" + payrolls.size() + " employees)");
            summaryLabel.setCellStyle(summaryStyle);

            Cell grossTotal = summaryRow.createCell(5);
            grossTotal.setCellValue(totalGross.doubleValue());
            grossTotal.setCellStyle(summaryMoneyStyle);

            Cell netTotal = summaryRow.createCell(11);
            netTotal.setCellValue(totalNet.doubleValue());
            netTotal.setCellStyle(summaryMoneyStyle);

            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Payroll Excel: " + e.getMessage(), e);
        }
    }
}
