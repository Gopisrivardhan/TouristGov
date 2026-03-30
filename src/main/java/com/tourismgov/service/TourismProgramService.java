package com.tourismgov.service;

import com.tourismgov.dto.ProgramRequest;
import com.tourismgov.dto.ProgramResponse;
import com.tourismgov.dto.ResourceRequest;
import com.tourismgov.dto.ResourceResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface TourismProgramService {
    // --- Program Management ---
    ProgramResponse createProgram(ProgramRequest request);
    ProgramResponse getProgramById(Long programId);
    List<ProgramResponse> getAllPrograms();
    Page<ProgramResponse> getProgramsPaged(int page, int size);
    ProgramResponse updateProgram(Long programId, ProgramRequest request); // NEW
    ProgramResponse updateProgramStatus(Long programId, String status);
    void deleteProgram(Long programId); // NEW

    // --- Resource Management ---
    ResourceResponse allocateResourceToProgram(Long programId, ResourceRequest request);
    ResourceResponse updateResourceStatus(Long resourceId, String status);
    List<ResourceResponse> getResourcesByProgram(Long programId);
    void deleteResource(Long resourceId); // NEW

    // --- Analytics ---
    Map<String, Object> getBudgetReport(Long programId);
}