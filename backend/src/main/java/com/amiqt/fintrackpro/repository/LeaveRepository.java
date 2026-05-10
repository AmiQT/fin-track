package com.amiqt.fintrackpro.repository;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    @Query("SELECT COALESCE(SUM(l.totalDays), 0) FROM LeaveRequest l WHERE l.employee.id = :employeeId AND l.leaveType = :leaveType AND l.status IN ('APPROVED', 'PENDING') AND EXTRACT(YEAR FROM l.startDate) = :year")
    Integer sumTakenDaysByEmployeeAndTypeAndYear(@Param("employeeId") Long employeeId, @Param("leaveType") LeaveType leaveType, @Param("year") int year);
}
