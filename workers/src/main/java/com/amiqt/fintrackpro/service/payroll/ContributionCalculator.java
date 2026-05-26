package com.amiqt.fintrackpro.service.payroll;

import com.amiqt.fintrackpro.model.entity.Employee;
import java.math.BigDecimal;

public interface ContributionCalculator {
    BigDecimal calculate(BigDecimal taxableSalary, Employee employee);
    String getContributionName();
}
