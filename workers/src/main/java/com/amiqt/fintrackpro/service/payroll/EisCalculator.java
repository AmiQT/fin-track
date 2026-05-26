package com.amiqt.fintrackpro.service.payroll;

import com.amiqt.fintrackpro.model.entity.Employee;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class EisCalculator implements ContributionCalculator {
    @Override
    public BigDecimal calculate(BigDecimal taxableSalary, Employee employee) {
        return taxableSalary.multiply(new BigDecimal("0.002")).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getContributionName() {
        return "EIS";
    }
    
    public BigDecimal calculateEmployerContribution(BigDecimal taxableSalary) {
        return calculate(taxableSalary, null);
    }
}
