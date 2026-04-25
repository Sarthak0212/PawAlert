package com.pawalert.controller;

import com.pawalert.dto.ReporterDashboardDTO;
import com.pawalert.dto.VolunteerDashboardDTO;
import com.pawalert.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") // For development
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/reporter/{userId}")
    public ResponseEntity<ReporterDashboardDTO> getReporterDashboard(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getReporterDashboard(userId));
    }

    @GetMapping("/volunteer/{userId}")
    public ResponseEntity<VolunteerDashboardDTO> getVolunteerDashboard(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getVolunteerDashboard(userId));
    }
}
