package com.tourismgov.repository;

import com.tourismgov.model.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

    // Logic: Find all records for a specific Site, Event, or Program
    List<ComplianceRecord> findByEntityIdAndType(Long entityId, String type);

    // Logic: Find all "FAILED" compliance records for the Compliance Officer Dashboard
    List<ComplianceRecord> findByResult(String result);

    // Logic: Get the most recent compliance history
    List<ComplianceRecord> findTop10ByOrderByDateDesc();
}