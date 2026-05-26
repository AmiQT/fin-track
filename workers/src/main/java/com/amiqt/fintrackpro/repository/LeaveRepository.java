package com.amiqt.fintrackpro.repository;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
}
