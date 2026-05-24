package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.response.DashboardResponse;
import com.amiqt.fintrackpro.model.dto.response.UserDashboardResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard analytics and summaries")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary() {
        log.debug("Dashboard summary requested");
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/my")
    public ResponseEntity<UserDashboardResponse> getMySummary(@AuthenticationPrincipal User user) {
        log.debug("Personal dashboard requested for: {}", user.getEmail());
        return ResponseEntity.ok(dashboardService.getMyDashboardSummary(user.getEmail()));
    }
}
