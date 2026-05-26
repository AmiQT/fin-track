package com.amiqt.fintrackpro.service.payroll;

import com.amiqt.fintrackpro.enums.MaritalStatus;
import com.amiqt.fintrackpro.model.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorsTest {

    private final EpfCalculator epfCalculator = new EpfCalculator();
    private final SocsoCalculator socsoCalculator = new SocsoCalculator();
    private final EisCalculator eisCalculator = new EisCalculator();
    private final PcbCalculator pcbCalculator = new PcbCalculator();

    @Test
    @DisplayName("EPF Calculator should compute correct employee & employer contributions")
    void testEpfCalculator() {
        Employee employee = new Employee();
        employee.setEpfRate(new BigDecimal("0.11"));
        
        BigDecimal salary = new BigDecimal("5000.00");
        BigDecimal result = epfCalculator.calculate(salary, employee);
        assertEquals(new BigDecimal("550.00"), result);
        
        BigDecimal employerResult = epfCalculator.calculateEmployerContribution(salary);
        assertEquals(new BigDecimal("650.00"), employerResult); // 5000 * 0.13 = 650

        // Test with default rate
        employee.setEpfRate(null);
        BigDecimal defaultResult = epfCalculator.calculate(salary, employee);
        assertEquals(new BigDecimal("550.00"), defaultResult);
        
        assertEquals("EPF", epfCalculator.getContributionName());
    }

    @Test
    @DisplayName("SOCSO Calculator should compute correct employee & employer contributions based on salary brackets")
    void testSocsoCalculator() {
        Employee employee = new Employee();
        
        // Under 30
        assertEquals(new BigDecimal("0.10"), socsoCalculator.calculate(new BigDecimal("20.00"), employee));
        
        // Under 50
        assertEquals(new BigDecimal("0.20"), socsoCalculator.calculate(new BigDecimal("40.00"), employee));
        
        // Under 4000
        BigDecimal sal1 = new BigDecimal("3000.00");
        assertEquals(new BigDecimal("15.00"), socsoCalculator.calculate(sal1, employee)); // 3000 * 0.005 = 15.00
        assertEquals(new BigDecimal("52.50"), socsoCalculator.calculateEmployerContribution(sal1)); // 3000 * 0.0175 = 52.50
        
        // Over 4000 (Capped)
        BigDecimal sal2 = new BigDecimal("5000.00");
        assertEquals(new BigDecimal("19.75"), socsoCalculator.calculate(sal2, employee));
        assertEquals(new BigDecimal("69.05"), socsoCalculator.calculateEmployerContribution(sal2));
        
        assertEquals("SOCSO", socsoCalculator.getContributionName());
    }

    @Test
    @DisplayName("EIS Calculator should compute correct employee & employer contributions")
    void testEisCalculator() {
        Employee employee = new Employee();
        BigDecimal salary = new BigDecimal("4000.00");
        
        BigDecimal result = eisCalculator.calculate(salary, employee);
        assertEquals(new BigDecimal("8.00"), result); // 4000 * 0.002 = 8.00
        
        BigDecimal employerResult = eisCalculator.calculateEmployerContribution(salary);
        assertEquals(new BigDecimal("8.00"), employerResult);
        
        assertEquals("EIS", eisCalculator.getContributionName());
    }

    @Test
    @DisplayName("PCB Calculator should compute correct tax deduction based on marital status and children")
    void testPcbCalculator() {
        Employee employee = new Employee();
        employee.setMaritalStatus(MaritalStatus.SINGLE);
        employee.setNumberOfChildren(0);
        
        // Adjusted Gross = 2000 (Taxable = 2000 - 750 = 1250 -> Under 2500)
        assertEquals(BigDecimal.ZERO, pcbCalculator.calculate(new BigDecimal("2000.00"), employee));
        
        // Adjusted Gross = 4000 (Taxable = 4000 - 750 = 3250 -> between 2500 and 4000)
        // Tax = (3250 - 2500) * 0.03 = 750 * 0.03 = 22.50
        assertEquals(new BigDecimal("22.50"), pcbCalculator.calculate(new BigDecimal("4000.00"), employee));
        
        // Married Spouse Not Working, with 2 children
        employee.setMaritalStatus(MaritalStatus.MARRIED_SPOUSE_NOT_WORKING);
        employee.setNumberOfChildren(2);
        // Adjusted Gross = 6000
        // Taxable = 6000 - 750 (personal) - 333 (spouse) - 166*2 (children) = 6000 - 1415 = 4585 (between 4000 and 6000)
        // Tax = 45 + (4585 - 4000) * 0.08 = 45 + 585 * 0.08 = 45 + 46.80 = 91.80
        assertEquals(new BigDecimal("91.80"), pcbCalculator.calculate(new BigDecimal("6000.00"), employee));
        
        // High Income Bracket (over 6000 taxable)
        employee.setMaritalStatus(MaritalStatus.SINGLE);
        employee.setNumberOfChildren(null);
        // Adjusted Gross = 8000
        // Taxable = 8000 - 750 = 7250
        // Tax = 205 + (7250 - 6000) * 0.13 = 205 + 1250 * 0.13 = 205 + 162.50 = 367.50
        assertEquals(new BigDecimal("367.50"), pcbCalculator.calculate(new BigDecimal("8000.00"), employee));
        
        assertEquals("PCB", pcbCalculator.getContributionName());
    }
}
