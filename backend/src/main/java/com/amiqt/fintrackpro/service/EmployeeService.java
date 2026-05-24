package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import com.amiqt.fintrackpro.enums.Role;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.mapper.EmployeeMapper;
import com.amiqt.fintrackpro.model.dto.request.EmployeeRequest;
import com.amiqt.fintrackpro.model.dto.response.EmployeeResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toResponse);
    }

    @Cacheable(value = "employee", key = "#id")
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating employee: {}", request.email());
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating an employee account");
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_EMPLOYEE)
                .build();
        User savedUser = userRepository.save(user);

        Employee employee = employeeMapper.toEntity(request);
        employee.setUser(savedUser);
        employee.setStatus(EmployeeStatus.ACTIVE);

        EmployeeResponse response = employeeMapper.toResponse(employeeRepository.save(employee));
        log.info("Employee created — id: {}, code: {}", response.id(), response.employeeCode());
        return response;
    }

    @Transactional
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Updating employee id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setFullName(request.fullName());
        employee.setPhone(request.phone());
        employee.setDepartment(request.department());
        employee.setPosition(request.position());
        employee.setBasicSalary(request.basicSalary());
        employee.setHousingAllowance(request.housingAllowance());
        employee.setTransportAllowance(request.transportAllowance());

        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void deleteEmployee(Long id) {
        log.info("Soft-deleting employee id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }

    public Page<EmployeeResponse> searchEmployees(String query, Pageable pageable) {
        log.debug("Searching employees with query: '{}'", query);
        return employeeRepository.searchEmployees(query, pageable)
                .map(employeeMapper::toResponse);
    }

    public EmployeeResponse getMyProfile(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));
        return employeeMapper.toResponse(employee);
    }

    public String exportEmployeesToCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee Code,Full Name,Email,Department,Position,Basic Salary,Status,Join Date\n");

        List<Employee> employees = employeeRepository.findAll(PageRequest.of(0, 10_000)).getContent();
        employees.forEach(emp -> {
            csv.append(String.format("%s,%s,%s,%s,%s,%.2f,%s,%s\n",
                    escapeCsvField(emp.getEmployeeCode()),
                    escapeCsvField(emp.getFullName()),
                    escapeCsvField(emp.getEmail()),
                    escapeCsvField(emp.getDepartment()),
                    escapeCsvField(emp.getPosition()),
                    emp.getBasicSalary() != null ? emp.getBasicSalary() : 0,
                    emp.getStatus(),
                    emp.getJoinDate()));
        });

        return csv.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        // Neutralize formula injection (=, +, -, @, TAB, CR)
        String sanitized = field.replaceAll("^[=+\\-@\t\r]", "'$0");
        // Wrap in quotes if it contains commas, quotes, or newlines
        if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n")) {
            sanitized = "\"" + sanitized.replace("\"", "\"\"") + "\"";
        }
        return sanitized;
    }

    public byte[] exportEmployeesToExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");

            // Header style — dark blue, bold white
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            ((XSSFCellStyle) headerStyle).setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 14, (byte) 79, (byte) 143}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            DataFormat dataFormat = workbook.createDataFormat();

            // Money style
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            // Summary style — green tint
            CellStyle summaryStyle = workbook.createCellStyle();
            XSSFFont summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            ((XSSFCellStyle) summaryStyle).setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 198, (byte) 224, (byte) 180}, null));
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle summaryMoneyStyle = workbook.createCellStyle();
            summaryMoneyStyle.cloneStyleFrom(summaryStyle);
            summaryMoneyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            // Header row
            String[] headers = {"No.", "Employee Code", "Full Name", "Email", "Department",
                    "Position", "Basic Salary (RM)", "Housing (RM)", "Transport (RM)", "Join Date", "Status"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows — capped at 10k to prevent OOM
            List<Employee> employees = employeeRepository.findAll(PageRequest.of(0, 10_000)).getContent();
            int rowNum = 1;
            BigDecimal totalBasic = BigDecimal.ZERO;

            for (Employee emp : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "");
                row.createCell(2).setCellValue(emp.getFullName() != null ? emp.getFullName() : "");
                row.createCell(3).setCellValue(emp.getEmail() != null ? emp.getEmail() : "");
                row.createCell(4).setCellValue(emp.getDepartment() != null ? emp.getDepartment() : "");
                row.createCell(5).setCellValue(emp.getPosition() != null ? emp.getPosition() : "");

                double basic = emp.getBasicSalary() != null ? emp.getBasicSalary().doubleValue() : 0.0;
                Cell salaryCell = row.createCell(6);
                salaryCell.setCellValue(basic);
                salaryCell.setCellStyle(moneyStyle);

                Cell housingCell = row.createCell(7);
                housingCell.setCellValue(emp.getHousingAllowance() != null ? emp.getHousingAllowance().doubleValue() : 0.0);
                housingCell.setCellStyle(moneyStyle);

                Cell transportCell = row.createCell(8);
                transportCell.setCellValue(emp.getTransportAllowance() != null ? emp.getTransportAllowance().doubleValue() : 0.0);
                transportCell.setCellStyle(moneyStyle);

                row.createCell(9).setCellValue(emp.getJoinDate() != null ? emp.getJoinDate().toString() : "");
                row.createCell(10).setCellValue(emp.getStatus() != null ? emp.getStatus().name() : "");

                if (emp.getBasicSalary() != null) totalBasic = totalBasic.add(emp.getBasicSalary());
            }

            // Summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            Cell summaryLabel = summaryRow.createCell(1);
            summaryLabel.setCellValue("TOTAL (" + employees.size() + " employees)");
            summaryLabel.setCellStyle(summaryStyle);
            Cell summaryTotal = summaryRow.createCell(6);
            summaryTotal.setCellValue(totalBasic.doubleValue());
            summaryTotal.setCellStyle(summaryMoneyStyle);

            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel: " + e.getMessage(), e);
        }
    }
}
