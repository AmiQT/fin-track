package com.amiqt.fintrackpro.mapper;

import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.entity.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    public PayrollResponse toResponse(Payroll payroll) {
        return new PayrollResponse(
                payroll.getId(),
                payroll.getEmployee().getId(),
                payroll.getEmployee().getFullName(),
                payroll.getMonth(),
                payroll.getYear(),
                payroll.getBasicSalary(),
                payroll.getGrossSalary(),
                payroll.getEpfEmployee(),
                payroll.getEpfEmployer(),
                payroll.getSocsoEmployee(),
                payroll.getSocsoEmployer(),
                payroll.getEisEmployee(),
                payroll.getEisEmployer(),
                payroll.getIncomeTax(),
                payroll.getUnpaidLeaveDeduction(),
                payroll.getTotalDeductions(),
                payroll.getNetSalary(),
                payroll.getProcessedAt()
        );
    }
}
