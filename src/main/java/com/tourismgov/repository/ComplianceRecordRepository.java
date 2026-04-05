package com.tourismgov.repository;

import com.tourismgov.model.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {
    List<ComplianceRecord> findByEntityIdAndType(Long entityId, String type);
    List<ComplianceRecord> findByResult(String result);
    List<ComplianceRecord> findTop10ByOrderByDateDesc();
}