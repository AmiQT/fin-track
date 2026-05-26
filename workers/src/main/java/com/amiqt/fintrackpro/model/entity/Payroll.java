package com.amiqt.fintrackpro.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "basic_salary")
    private BigDecimal basicSalary;

    @Column(name = "housing_allowance")
    private BigDecimal housingAllowance;

    @Column(name = "transport_allowance")
    private BigDecimal transportAllowance;

    @Column(name = "gross_salary")
    private BigDecimal grossSalary;

    @Column(name = "epf_employee")
    private BigDecimal epfEmployee;

    @Column(name = "epf_employer")
    private BigDecimal epfEmployer;

    @Column(name = "socso_employee")
    private BigDecimal socsoEmployee;

    @Column(name = "socso_employer")
    private BigDecimal socsoEmployer;

    @Column(name = "eis_employee")
    private BigDecimal eisEmployee;

    @Column(name = "eis_employer")
    private BigDecimal eisEmployer;

    @Column(name = "income_tax")
    private BigDecimal incomeTax;

    @Column(name = "unpaid_leave_deduction")
    private BigDecimal unpaidLeaveDeduction;

    @Column(name = "total_deductions")
    private BigDecimal totalDeductions;

    @Column(name = "net_salary")
    private BigDecimal netSalary;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
