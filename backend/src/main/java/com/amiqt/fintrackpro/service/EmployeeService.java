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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 1. Create User first
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password() != null ? request.password() : "password123"))
                .role(Role.ROLE_EMPLOYEE)
                .build();
        User savedUser = userRepository.save(user);

        // 2. Create Employee
        Employee employee = employeeMapper.toEntity(request);
        employee.setUser(savedUser);
        employee.setStatus(EmployeeStatus.ACTIVE);
        
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
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
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setStatus(EmployeeStatus.INACTIVE); // Soft delete
        employeeRepository.save(employee);
    }

    public Page<EmployeeResponse> searchEmployees(String query, Pageable pageable) {
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

        employeeRepository.findAll().forEach(emp -> {
            csv.append(String.format("%s,%s,%s,%s,%s,%.2f,%s,%s\n",
                    emp.getEmployeeCode(),
                    emp.getFullName(),
                    emp.getEmail(),
                    emp.getDepartment(),
                    emp.getPosition(),
                    emp.getBasicSalary(),
                    emp.getStatus(),
                    emp.getJoinDate()));
        });

        return csv.toString();
    }
}
