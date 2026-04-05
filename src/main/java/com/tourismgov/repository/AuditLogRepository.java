package com.tourismgov.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourismgov.model.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds all audit logs for a specific user.
     * The underscore (User_UserId) tells Spring to look inside the User object for the userId.
     */
    Page<AuditLog> findByUser_UserId(Long userId, Pageable pageable);

    /**
     * Finds all audit logs that occurred between two specific dates/times.
     * Crucial for Administrator investigations.
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Finds all audit logs for a specific action (e.g., "DELETE_SITE" or "USER_LOGIN").
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    /**
     * Optional but highly recommended for Admin dashboards: 
     * Find logs by status (e.g., to find all "FAILED" login attempts).
     */
    Page<AuditLog> findByStatus(String status, Pageable pageable);
    
    
}