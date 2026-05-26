package com.amiqt.fintrackpro.service.payroll;

import com.amiqt.fintrackpro.model.entity.Employee;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class EpfCalculator implements ContributionCalculator {
    @Override
    public BigDecimal calculate(BigDecimal taxableSalary, Employee employee) {
        BigDecimal epfRate = employee.getEpfRate() != null ? employee.getEpfRate() : new BigDecimal("0.11");
        return taxableSalary.multiply(epfRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getContributionName() {
        return "EPF";
    }
    
    public BigDecimal calculateEmployerContribution(BigDecimal taxableSalary) {
        return taxableSalary.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
    }
}
