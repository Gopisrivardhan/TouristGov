package com.tourismgov.service;

import com.tourismgov.dto.ProgramRequest;
import com.tourismgov.dto.ProgramResponse;
import com.tourismgov.dto.ResourceRequest;
import com.tourismgov.dto.ResourceResponse;
import com.tourismgov.model.Resource;
import com.tourismgov.model.TourismProgram;
import com.tourismgov.repository.ResourceRepository;
import com.tourismgov.repository.TourismProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourismProgramServiceImpl implements TourismProgramService {

    private static final String PROGRAM_NOT_FOUND = "Program not found";

    private final TourismProgramRepository programRepository;
    private final ResourceRepository resourceRepository;

    @Override
    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        log.info("Creating new Tourism Program: {}", request.getTitle());
        
        TourismProgram program = new TourismProgram();
        program.setTitle(request.getTitle());
        program.setDescription(request.getDescription());
        program.setStartDate(request.getStartDate());
        program.setEndDate(request.getEndDate());
        program.setBudget(request.getBudget());
        program.setStatus("PLANNED");

        TourismProgram savedProgram = programRepository.save(program);
        return mapToProgramResponse(savedProgram);
    }

    @Override
    public ProgramResponse getProgramById(Long programId) {
        return programRepository.findById(programId)
                .map(this::mapToProgramResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND));
    }

    @Override
    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll().stream().map(this::mapToProgramResponse).toList();
    }

    @Override
    public Page<ProgramResponse> getProgramsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return programRepository.findAll(pageable).map(this::mapToProgramResponse);
    }

    // NEW
    @Override
    @Transactional
    public ProgramResponse updateProgram(Long programId, ProgramRequest request) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND));

        program.setTitle(request.getTitle());
        program.setDescription(request.getDescription());
        program.setStartDate(request.getStartDate());
        program.setEndDate(request.getEndDate());
        program.setBudget(request.getBudget());

        return mapToProgramResponse(programRepository.save(program));
    }

    @Override
    @Transactional
    public ProgramResponse updateProgramStatus(Long programId, String status) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND));
        
        program.setStatus(status.toUpperCase());
        return mapToProgramResponse(programRepository.save(program));
    }

    // NEW
    @Override
    @Transactional
    public void deleteProgram(Long programId) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND));
        programRepository.delete(program);
    }

    @Override
    @Transactional
    public ResourceResponse allocateResourceToProgram(Long programId, ResourceRequest request) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND));

        Resource resource = new Resource();
        resource.setProgram(program);
        resource.setType(request.getType().toUpperCase()); // Standardize to uppercase for easier filtering
        resource.setQuantity(request.getQuantity());
        resource.setStatus("ALLOCATED");

        Resource savedResource = resourceRepository.save(resource);
        return mapToResourceResponse(savedResource);
    }

    @Override
    @Transactional
    public ResourceResponse updateResourceStatus(Long resourceId, String status) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        
        resource.setStatus(status.toUpperCase());
        return mapToResourceResponse(resourceRepository.save(resource));
    }

    @Override
    public List<ResourceResponse> getResourcesByProgram(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND);
        }
        return resourceRepository.findByProgram_ProgramId(programId).stream()
                .map(this::mapToResourceResponse)
                .toList();
    }

    // NEW
    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        resourceRepository.delete(resource);
    }

    @Override
    public Map<String, Object> getBudgetReport(Long programId) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROGRAM_NOT_FOUND));
        
        List<Resource> resources = resourceRepository.findByProgram_ProgramId(programId);
        
        // FIXED LOGIC: Only calculate the cost of resources that are explicitly "FUNDS"
        double totalSpent = resources.stream()
                .filter(r -> "FUNDS".equalsIgnoreCase(r.getType()))
                .mapToDouble(Resource::getQuantity)
                .sum();
                
        double budget = program.getBudget() != null ? program.getBudget() : 0.0;
        double remaining = budget - totalSpent;
        
        return Map.of(
            "programTitle", program.getTitle(),
            "allocatedBudget", budget,
            "totalFundsSpent", totalSpent,
            "remainingBudget", remaining,
            "isOverBudget", totalSpent > budget
        );
    }

    // --- Private Mapping Helpers ---
    private ProgramResponse mapToProgramResponse(TourismProgram program) {
        ProgramResponse response = new ProgramResponse();
        response.setProgramId(program.getProgramId());
        response.setTitle(program.getTitle());
        response.setDescription(program.getDescription());
        response.setStartDate(program.getStartDate());
        response.setEndDate(program.getEndDate());
        response.setBudget(program.getBudget());
        response.setStatus(program.getStatus());
        return response;
    }

    private ResourceResponse mapToResourceResponse(Resource resource) {
        ResourceResponse response = new ResourceResponse();
        response.setResourceId(resource.getResourceId());
        if (resource.getProgram() != null) {
            response.setProgramId(resource.getProgram().getProgramId());
        }
        response.setType(resource.getType());
        response.setQuantity(resource.getQuantity());
        response.setStatus(resource.getStatus());
        return response;
    }
}