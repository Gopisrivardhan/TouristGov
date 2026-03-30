package com.tourismgov.repository;

import com.tourismgov.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Booking, Long> {

    // --- COMMON DATA ---
    @Query("SELECT COUNT(s) FROM HeritageSite s")
    long countTotalSites();

    @Query("SELECT COUNT(s) FROM HeritageSite s WHERE s.status = 'ACTIVE'")
    long countActiveHeritageSites();

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'ACTIVE'")
    long countActiveEvents();

    @Query("SELECT COALESCE(SUM(p.budget), 0.0) FROM TourismProgram p")
    Double sumTotalProgramBudget();

    // --- 1. TOURIST ---
    @Query("SELECT COALESCE(SUM(100.0), 0.0) FROM Booking b WHERE b.tourist.touristId = :id AND b.status = 'CONFIRMED'") 
    Double sumAmountByUserId(@Param("id") Long id);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tourist.touristId = :id AND b.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("id") Long id);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tourist.touristId = :id")
    long countTotalBookingHistory(@Param("id") Long id);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tourist.touristId = :id AND b.status = 'CONFIRMED'")
    long countUpcomingEvents(@Param("id") Long id);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :id AND n.status = 'UNREAD'")
    long countMyUnreadNotifications(@Param("id") Long id);
    
    @Query("SELECT COALESCE(MAX(d.verificationStatus), 'NOT_SUBMITTED') FROM TouristDocument d WHERE d.tourist.touristId = :id")
    String getMyDocumentStatus(@Param("id") Long id);

    // --- 2. INTERNAL ROLES ---
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countTotalActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countUsersByRole(@Param("role") String role);

    @Query("SELECT COUNT(d) FROM TouristDocument d WHERE d.verificationStatus = 'PENDING'")
    long countPendingDocValidations();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'")
    long countPendingBookingApprovals();

    @Query("SELECT COUNT(p) FROM TourismProgram p WHERE p.status = 'ACTIVE'")
    long countActivePrograms();

    // --- 3. COMPLIANCE & AUDIT ---
    @Query("SELECT COUNT(c) FROM ComplianceRecord c WHERE c.result = 'FAIL'")
    long countPolicyViolations();

    @Query("SELECT COUNT(a) FROM Audit a WHERE a.status = 'PENDING'")
    long countPendingAudits();

    @Query("SELECT COUNT(r) FROM Report r WHERE r.generatedDate >= CURRENT_DATE")
    long countWeeklyReportsGenerated();

    // FIXED: Changed ProgramResource to Resource to match your actual Entity
    @Query("SELECT COUNT(r) FROM Resource r WHERE r.quantity < 10")
    long countLowResourcesAlert();
}