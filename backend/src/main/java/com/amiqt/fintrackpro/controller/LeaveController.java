package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.LeaveRequestDto;
import com.amiqt.fintrackpro.model.dto.response.LeaveResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.AuditLogService;
import com.amiqt.fintrackpro.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for applying, approving, and exporting leave requests")
public class LeaveController {

    private final LeaveService leaveService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Apply for leave")
    public ResponseEntity<LeaveResponse> applyLeave(
            @Valid @RequestBody LeaveRequestDto request,
            @AuthenticationPrincipal User user
    ) {
        log.info("Leave application by: {} — type: {}", user.getEmail(), request.leaveType());
        return ResponseEntity.ok(leaveService.applyLeave(request, user.getEmail()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user leave history")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(@AuthenticationPrincipal User user) {
        log.debug("Leave history request for: {}", user.getEmail());
        return ResponseEntity.ok(leaveService.getMyLeaves(user.getEmail()));
    }

    @GetMapping("/balance")
    @Operation(summary = "Get current user leave balance")
    public ResponseEntity<com.amiqt.fintrackpro.model.dto.response.LeaveBalanceResponse> getLeaveBalance(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(user.getEmail()));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending leave requests (HR/Admin)")
    public ResponseEntity<Page<LeaveResponse>> getPendingLeaves(
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getPendingLeaves(pageable));
    }

    @GetMapping
    @Operation(summary = "Get all leave requests (HR/Admin)")
    public ResponseEntity<Page<LeaveResponse>> getAllLeaves(
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeaves(pageable));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export leave report to Excel")
    public ResponseEntity<byte[]> exportLeavesToExcel() {
        log.info("Exporting leaves to Excel");
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=leave_report.xlsx")
                .body(leaveService.exportLeavesToExcel());
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<LeaveResponse> approveLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest
    ) {
        log.info("Leave id {} approved by: {}", id, user.getEmail());
        LeaveResponse result = leaveService.approveLeave(id, user);
        auditLogService.log(user.getEmail(), "APPROVE_LEAVE", "LeaveRequest",
                String.valueOf(id), null, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<LeaveResponse> rejectLeave(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest
    ) {
        log.info("Leave id {} rejected by: {}", id, user.getEmail());
        LeaveResponse result = leaveService.rejectLeave(id, body.get("reason"), user);
        auditLogService.log(user.getEmail(), "REJECT_LEAVE", "LeaveRequest",
                String.valueOf(id), body.get("reason"), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(result);
    }
}
