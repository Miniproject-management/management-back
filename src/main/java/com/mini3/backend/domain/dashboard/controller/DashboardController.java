package com.mini3.backend.domain.dashboard.controller;

import com.mini3.backend.domain.dashboard.dto.AdminDashboardResponse;
import com.mini3.backend.domain.dashboard.dto.LeaderDashboardResponse;
import com.mini3.backend.domain.dashboard.dto.UserDashboardResponse;
import com.mini3.backend.domain.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/user")
    public ResponseEntity<UserDashboardResponse> getUserDashboard(@RequestParam Long empNo) {
        return ResponseEntity.ok(dashboardService.getUserDashboard(empNo));
    }

    @GetMapping("/leader")
    public ResponseEntity<LeaderDashboardResponse> getLeaderDashboard(@RequestParam Long empNo) {
        return ResponseEntity.ok(dashboardService.getLeaderDashboard(empNo));
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}
