package com.amiqt.fintrackpro.model.entity;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "employee_code", unique = true, nullable = true)
    private String employeeCode;

    @Column(name = "full_name", nullable = true)
    private String fullName;

    @Column(unique = true, nullable = true)
    private String email;

    private String phone;

    @Column(nullable = true)
    private String department;

    @Column(nullable = true)
    private String position;

    @Column(name = "basic_salary", nullable = true)
    private BigDecimal basicSalary;

    @Column(name = "housing_allowance")
    private BigDecimal housingAllowance;

    @Column(name = "transport_allowance")
    private BigDecimal transportAllowance;

    @Column(name = "join_date", nullable = true)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private com.amiqt.fintrackpro.enums.MaritalStatus maritalStatus;

    @Column(name = "number_of_children")
    private Integer numberOfChildren;

    @Column(name = "epf_rate")
    private BigDecimal epfRate;

    @Column(name = "annual_leave_entitlement")
    private Integer annualLeaveEntitlement;

    @Column(name = "sick_leave_entitlement")
    private Integer sickLeaveEntitlement;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = EmployeeStatus.ACTIVE;
        if (housingAllowance == null) housingAllowance = BigDecimal.ZERO;
        if (transportAllowance == null) transportAllowance = BigDecimal.ZERO;
        if (annualLeaveEntitlement == null) annualLeaveEntitlement = 14;
        if (sickLeaveEntitlement == null) sickLeaveEntitlement = 14;
        if (maritalStatus == null) maritalStatus = com.amiqt.fintrackpro.enums.MaritalStatus.SINGLE;
        if (numberOfChildren == null) numberOfChildren = 0;
        if (epfRate == null) epfRate = new BigDecimal("0.11");
    }
}
