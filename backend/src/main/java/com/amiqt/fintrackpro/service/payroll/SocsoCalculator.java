package com.amiqt.fintrackpro.service.payroll;

import com.amiqt.fintrackpro.model.entity.Employee;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SocsoCalculator implements ContributionCalculator {
    @Override
    public BigDecimal calculate(BigDecimal taxableSalary, Employee employee) {
        double sal = taxableSalary.doubleValue();
        if (sal <= 30) return new BigDecimal("0.10");
        if (sal <= 50) return new BigDecimal("0.20");
        if (sal <= 4000) return taxableSalary.multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal("19.75");
    }

    @Override
    public String getContributionName() {
        return "SOCSO";
    }

    public BigDecimal calculateEmployerContribution(BigDecimal taxableSalary) {
        double sal = taxableSalary.doubleValue();
        if (sal <= 4000) return taxableSalary.multiply(new BigDecimal("0.0175")).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal("69.05");
    }
}
