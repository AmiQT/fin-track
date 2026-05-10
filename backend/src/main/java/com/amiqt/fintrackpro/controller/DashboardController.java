package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.response.DashboardResponse;
import com.amiqt.fintrackpro.model.dto.response.UserDashboardResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/my")
    public ResponseEntity<UserDashboardResponse> getMySummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getMyDashboardSummary(user.getEmail()));
    }
}
