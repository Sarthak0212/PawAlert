package com.pawalert.controller;

import com.pawalert.dto.UserProfileDTO;
import com.pawalert.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getProfile(userId));
    }
}
