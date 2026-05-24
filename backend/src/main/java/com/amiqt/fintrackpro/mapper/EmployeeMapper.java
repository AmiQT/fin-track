package com.amiqt.fintrackpro.mapper;

import com.amiqt.fintrackpro.model.dto.request.EmployeeRequest;
import com.amiqt.fintrackpro.model.dto.response.EmployeeResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        return Employee.builder()
                .employeeCode(request.employeeCode())
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .department(request.department())
                .position(request.position())
                .basicSalary(request.basicSalary())
                .housingAllowance(request.housingAllowance())
                .transportAllowance(request.transportAllowance())
                .joinDate(request.joinDate())
                .maritalStatus(request.maritalStatus())
                .numberOfChildren(request.numberOfChildren())
                .epfRate(request.epfRate())
                .build();
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getPosition(),
                employee.getBasicSalary(),
                employee.getHousingAllowance(),
                employee.getTransportAllowance(),
                employee.getJoinDate(),
                employee.getStatus(),
                employee.getMaritalStatus() != null ? employee.getMaritalStatus().name() : null,
                employee.getNumberOfChildren(),
                employee.getEpfRate()
        );
    }
}
