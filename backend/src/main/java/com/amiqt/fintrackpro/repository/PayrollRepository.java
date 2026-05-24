package com.amiqt.fintrackpro.repository;

import com.amiqt.fintrackpro.model.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
    List<Payroll> findByEmployeeId(Long employeeId);
    List<Payroll> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);
    Page<Payroll> findByEmployeeId(Long employeeId, Pageable pageable);
    List<Payroll> findByMonthAndYear(Integer month, Integer year);
}
