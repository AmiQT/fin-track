package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.mapper.EmployeeMapper;
import com.amiqt.fintrackpro.model.dto.request.EmployeeRequest;
import com.amiqt.fintrackpro.model.dto.response.EmployeeResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Long id = 1L;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(id);
        employee.setFullName("Noor Amin");
        employee.setEmail("noor@fintrack.com");
    }

    @Test
    @DisplayName("Should return employee when ID exists")
    void getEmployeeByIdTest() {
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        EmployeeResponse response = new EmployeeResponse(id, "EMP001", "Noor Amin", "noor@fintrack.com", null, "IT", "Dev", BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO, null, EmployeeStatus.ACTIVE, "SINGLE", 0, new BigDecimal("0.11"));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.getEmployeeById(id);

        assertNotNull(result);
        assertEquals("Noor Amin", result.fullName());
        verify(employeeRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when employee not found")
    void getEmployeeByIdNotFoundTest() {
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    @DisplayName("Should soft delete employee by changing status to INACTIVE")
    void deleteEmployeeTest() {
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(id);

        assertEquals(EmployeeStatus.INACTIVE, employee.getStatus());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Should update employee fields correctly")
    void updateEmployeeTest() {
        EmployeeRequest request = new EmployeeRequest(
                "EMP001", "Noor Updated", "noor@fintrack.com", "012-9999999",
                "HR", "Manager",
                BigDecimal.valueOf(7000), BigDecimal.valueOf(600), BigDecimal.valueOf(400),
                null, null, null, null, null
        );
        EmployeeResponse expected = new EmployeeResponse(
                id, "EMP001", "Noor Updated", "noor@fintrack.com",
                "012-9999999", "HR", "Manager",
                BigDecimal.valueOf(7000), BigDecimal.valueOf(600), BigDecimal.valueOf(400),
                null, EmployeeStatus.ACTIVE, "SINGLE", 0, new BigDecimal("0.11")
        );

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.toResponse(any())).thenReturn(expected);

        EmployeeResponse result = employeeService.updateEmployee(id, request);

        assertNotNull(result);
        assertEquals("Noor Updated", result.fullName());
        assertEquals("HR", result.department());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent employee")
    void updateEmployeeNotFoundTest() {
        EmployeeRequest request = new EmployeeRequest(
                "EMP999", "Ghost", "ghost@fintrack.com", null,
                "IT", "Dev",
                BigDecimal.valueOf(3000), BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null, null
        );
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, request));
    }
}
