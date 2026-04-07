package com.tourismgov.repository;

import com.tourismgov.model.PreservationActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PreservationActivityRepository extends JpaRepository<PreservationActivity, Long> {
    List<PreservationActivity> findBySite_SiteId(Long siteId);
}