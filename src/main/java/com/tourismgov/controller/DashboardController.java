package com.tourismgov.controller;

import com.tourismgov.dto.DashboardDTO;
import com.tourismgov.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tourismgov/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Crucial for your React frontend connection
public class DashboardController {

    private final DashboardService dashboardService;

    
    @GetMapping("/stats")
    public ResponseEntity<DashboardDTO> getDashboardStats(
            @RequestParam String role, 
            @RequestParam Long userId) {
        
        return ResponseEntity.ok(dashboardService.getDashboardMetrics(role, userId));
    }
}