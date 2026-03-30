package com.tourismgov.service;

import com.tourismgov.dto.DashboardDTO;
import com.tourismgov.model.User;
import com.tourismgov.repository.DashboardRepository;
import com.tourismgov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepo;
    private final UserRepository userRepository;

    @Override
    public DashboardDTO getDashboardMetrics(String role, Long userId) {
        // 1. Fetch the REAL user from the Database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. DUAL-FACTOR VALIDATION: Compare URL role with Database role
        String dbRole = user.getRole().toUpperCase();
        String providedRole = role.toUpperCase();

        if (!dbRole.equals(providedRole)) {
            // Logic: Role and UserID MUST match or we kill the request
            throw new IllegalArgumentException("Access Denied: Role mismatch for User ID " + userId);
        }

        Map<String, Object> metrics = new LinkedHashMap<>();

        // --- COMMON DATA POINTS (Optional: Keep or remove based on UI needs) ---
        long totalS = dashboardRepo.countTotalSites();
        long activeS = dashboardRepo.countActiveHeritageSites();
        
        metrics.put("totalHeritageSites", totalS);
        metrics.put("activeSitePercentage", calculatePercentage(activeS, totalS) + "%");
        metrics.put("totalActiveEvents", dashboardRepo.countActiveEvents());
        metrics.put("totalProgramBudget", "₹" + dashboardRepo.sumTotalProgramBudget());

        // --- ROLE-SPECIFIC DATA POINTS ---
        // We use 'dbRole' here because we've verified it matches 'providedRole'
        switch (dbRole) {
            case "TOURIST":
                long myTotal = dashboardRepo.countTotalBookingHistory(userId);
                long myDone = dashboardRepo.countCompletedByUserId(userId);
                metrics.put("myTotalSpent", "₹" + dashboardRepo.sumAmountByUserId(userId));
                metrics.put("tripCompletionRate", calculatePercentage(myDone, myTotal) + "%");
                metrics.put("upcomingEvents", dashboardRepo.countUpcomingEvents(userId));
                metrics.put("documentStatus", dashboardRepo.getMyDocumentStatus(userId));
                break;

            case "OFFICER":
                metrics.put("pendingBookings", dashboardRepo.countPendingBookingApprovals());
                metrics.put("docsToVerify", dashboardRepo.countPendingDocValidations());
                metrics.put("activeSites", activeS);
                metrics.put("totalUsers", dashboardRepo.countTotalActiveUsers());
                break;

            case "MANAGER":
                metrics.put("activePrograms", dashboardRepo.countActivePrograms());
                metrics.put("lowResourceAlerts", dashboardRepo.countLowResourcesAlert());
                metrics.put("totalStaff", dashboardRepo.countUsersByRole("OFFICER"));
                break;

            case "ADMIN":
                metrics.put("totalUsers", dashboardRepo.countTotalActiveUsers());
                metrics.put("pendingApprovals", dashboardRepo.countPendingBookingApprovals());
                metrics.put("activePrograms", dashboardRepo.countActivePrograms());
                metrics.put("totalHeritageSites", totalS);
                break;

            default:
                throw new IllegalArgumentException("Invalid Role: " + dbRole);
        }

        // 3. THE BUILDER (LastLogin removed)
        return DashboardDTO.builder()
                .role(dbRole)
                .userId(userId)
                .userName(user.getName())
                .metrics(metrics)
                .unreadNotifications(dashboardRepo.countMyUnreadNotifications(userId))
                .build();
    }

    private double calculatePercentage(long part, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) part / total * 100.0 * 10.0) / 10.0;
    }
}