package com.tourismgov.repository;

import com.tourismgov.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    
    // Notice the underscore! It tells Spring to look inside the linked 'officer' object for 'userId'
    List<Audit> findByOfficer_UserId(Long officerId);
}