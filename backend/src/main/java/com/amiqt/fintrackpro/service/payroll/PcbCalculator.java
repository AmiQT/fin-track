package com.amiqt.fintrackpro.service.payroll;

import com.amiqt.fintrackpro.enums.MaritalStatus;
import com.amiqt.fintrackpro.model.entity.Employee;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PcbCalculator implements ContributionCalculator {
    @Override
    public BigDecimal calculate(BigDecimal adjustedGross, Employee employee) {
        BigDecimal taxableIncome = adjustedGross;
        
        // Personal Relief
        taxableIncome = taxableIncome.subtract(new BigDecimal("750"));
        
        // Marital Status Relief
        if (employee.getMaritalStatus() == MaritalStatus.MARRIED_SPOUSE_NOT_WORKING) {
            taxableIncome = taxableIncome.subtract(new BigDecimal("333"));
        }
        
        // Children Relief
        int children = employee.getNumberOfChildren() != null ? employee.getNumberOfChildren() : 0;
        taxableIncome = taxableIncome.subtract(new BigDecimal("166").multiply(new BigDecimal(children)));

        if (taxableIncome.compareTo(new BigDecimal("2500")) <= 0) {
            return BigDecimal.ZERO;
        }

        if (taxableIncome.compareTo(new BigDecimal("4000")) <= 0) {
            return taxableIncome.subtract(new BigDecimal("2500")).multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP);
        } else if (taxableIncome.compareTo(new BigDecimal("6000")) <= 0) {
            return new BigDecimal("45").add(taxableIncome.subtract(new BigDecimal("4000")).multiply(new BigDecimal("0.08"))).setScale(2, RoundingMode.HALF_UP);
        } else {
            return new BigDecimal("205").add(taxableIncome.subtract(new BigDecimal("6000")).multiply(new BigDecimal("0.13"))).setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String getContributionName() {
        return "PCB";
    }
}
