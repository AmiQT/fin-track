package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.LeaveRequestDto;
import com.amiqt.fintrackpro.model.dto.response.LeaveResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<LeaveResponse> applyLeave(
            @Valid @RequestBody LeaveRequestDto request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(leaveService.applyLeave(request, user.getEmail()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaveService.getMyLeaves(user.getEmail())); 
    }

    @GetMapping("/balance")
    public ResponseEntity<com.amiqt.fintrackpro.model.dto.response.LeaveBalanceResponse> getLeaveBalance(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(user.getEmail()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(leaveService.approveLeave(id, user));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeave(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(leaveService.rejectLeave(id, body.get("reason"), user));
    }
}
