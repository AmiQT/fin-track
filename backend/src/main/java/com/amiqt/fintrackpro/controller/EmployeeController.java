package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.EmployeeRequest;
import com.amiqt.fintrackpro.model.dto.response.EmployeeResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for managing employee profiles, searching, and exporting data")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in employee profile")
    public ResponseEntity<EmployeeResponse> getMyProfile(@AuthenticationPrincipal User user) {
        log.debug("Profile request for: {}", user.getEmail());
        return ResponseEntity.ok(employeeService.getMyProfile(user.getEmail()));
    }

    @GetMapping
    @Operation(summary = "Get all employees (paginated)", description = "Requires ADMIN role")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(Pageable pageable) {
        log.debug("Get all employees — page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        log.debug("Get employee by id: {}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new employee", description = "Creates both Employee record and corresponding User account for login")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        log.info("Creating employee: {}", request.email());
        EmployeeResponse response = employeeService.createEmployee(request);
        log.info("Employee created with id: {}", response.id());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee details")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        log.info("Updating employee id: {}", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee (soft delete)")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("Soft-deleting employee id: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees by name or code")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @RequestParam String query,
            Pageable pageable
    ) {
        log.debug("Searching employees: '{}'", query);
        return ResponseEntity.ok(employeeService.searchEmployees(query, pageable));
    }

    @GetMapping("/export")
    @Operation(summary = "Export all employees to CSV")
    public ResponseEntity<byte[]> exportEmployeesToCsv() {
        log.info("Exporting employees to CSV");
        String csv = employeeService.exportEmployeesToCsv();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=employees.csv")
                .body(csv.getBytes());
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export all employees to Excel")
    public ResponseEntity<byte[]> exportEmployeesToExcel() {
        log.info("Exporting employees to Excel");
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=employees.xlsx")
                .body(employeeService.exportEmployeesToExcel());
    }
}
