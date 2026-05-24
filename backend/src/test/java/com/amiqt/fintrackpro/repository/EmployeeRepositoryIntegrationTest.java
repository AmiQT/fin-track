package com.amiqt.fintrackpro.repository;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import com.amiqt.fintrackpro.model.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class EmployeeRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee buildEmployee(String code, String name, String email, String department) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFullName(name);
        e.setEmail(email);
        e.setDepartment(department);
        e.setPosition("Developer");
        e.setBasicSalary(BigDecimal.valueOf(5000));
        e.setStatus(EmployeeStatus.ACTIVE);
        return e;
    }

    @Test
    @DisplayName("Should persist employee and retrieve by ID")
    void saveAndFindByIdTest() {
        Employee saved = employeeRepository.save(
                buildEmployee("EMP001", "Noor Amin", "noor@fintrack.com", "Engineering"));

        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getEmployeeCode());
        assertEquals("noor@fintrack.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should find employee by email")
    void findByEmailTest() {
        employeeRepository.save(
                buildEmployee("EMP002", "Siti Rahimah", "siti@fintrack.com", "HR"));

        Optional<Employee> found = employeeRepository.findByEmail("siti@fintrack.com");

        assertTrue(found.isPresent());
        assertEquals("Siti Rahimah", found.get().getFullName());
    }

    @Test
    @DisplayName("Should return empty Optional for unknown email")
    void findByEmailNotFoundTest() {
        Optional<Employee> found = employeeRepository.findByEmail("ghost@fintrack.com");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should search employees by name using custom JPQL — case insensitive")
    void searchEmployeesByNameTest() {
        employeeRepository.save(
                buildEmployee("EMP003", "Ahmad Faris", "faris@fintrack.com", "Finance"));
        employeeRepository.save(
                buildEmployee("EMP004", "Zulaikha Binti Ali", "zulaikha@fintrack.com", "IT"));

        Page<Employee> results = employeeRepository.searchEmployees("ahmad", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Ahmad Faris", results.getContent().get(0).getFullName());
    }

    @Test
    @DisplayName("Should search employees by department using custom JPQL")
    void searchEmployeesByDepartmentTest() {
        employeeRepository.save(
                buildEmployee("EMP005", "Haziq Danial", "haziq@fintrack.com", "Engineering"));
        employeeRepository.save(
                buildEmployee("EMP006", "Liyana Putri", "liyana@fintrack.com", "Marketing"));

        Page<Employee> results = employeeRepository.searchEmployees("Engineering", PageRequest.of(0, 10));

        assertTrue(results.getTotalElements() >= 1);
        assertTrue(results.getContent().stream()
                .anyMatch(e -> "Engineering".equals(e.getDepartment())));
    }

    @Test
    @DisplayName("Should return paginated results with correct page size")
    void paginationTest() {
        for (int i = 7; i <= 12; i++) {
            employeeRepository.save(
                    buildEmployee("EMP0" + i, "Employee " + i, "emp" + i + "@fintrack.com", "IT"));
        }

        Page<Employee> firstPage = employeeRepository.findAll(PageRequest.of(0, 3));
        Page<Employee> secondPage = employeeRepository.findAll(PageRequest.of(1, 3));

        assertEquals(3, firstPage.getContent().size());
        assertTrue(firstPage.getTotalElements() >= 6);
        assertNotEquals(firstPage.getContent().get(0).getId(),
                secondPage.getContent().get(0).getId());
    }
}
