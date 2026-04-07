package com.tourismgov.service;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.autosender.GlobalActivityEvent;
import com.tourismgov.dto.ProgramRequest;
import com.tourismgov.dto.ProgramResponse;
import com.tourismgov.dto.ResourceRequest;
import com.tourismgov.dto.ResourceResponse;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.exception.ErrorMessages;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.Resource;
import com.tourismgov.model.TourismProgram;
import com.tourismgov.repository.HeritageSiteRepository;
import com.tourismgov.repository.ResourceRepository;
import com.tourismgov.repository.TourismProgramRepository;
import com.tourismgov.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourismProgramServiceImpl implements TourismProgramService {

    private static final String RES_PROGRAM = "TOURISM_PROGRAM";
    private static final String RES_RESOURCE = "PROGRAM_RESOURCE";
    private static final String ACT_CREATE = "CREATE";
    private static final String ACT_UPDATE = "UPDATE";
    private static final String ACT_DELETE = "DELETE";
    private static final String ACT_STATUS_CHANGE = "STATUS_CHANGE";
    private static final String ACT_ALLOCATE = "ALLOCATE";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private final TourismProgramRepository programRepository;
    private final ResourceRepository resourceRepository;
    private final HeritageSiteRepository siteRepository;
    private final AuditLogService auditLogService; 
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        log.info("Creating new Tourism Program: {}", request.getTitle());
        
        TourismProgram program = new TourismProgram();
        mapRequestToEntity(request, program);
        program.setStatus("PLANNED");

        if (request.getHeritageSiteIds() != null) {
            List<HeritageSite> sites = siteRepository.findAllById(request.getHeritageSiteIds());
            program.setHeritageSites(sites);
        }

        TourismProgram savedProgram = programRepository.save(program);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACT_CREATE, RES_PROGRAM, STATUS_SUCCESS);

        // TRIGGER GLOBAL NOTIFICATION
        String message = String.format("The Ministry has launched a new tourism program: '%s'. Explore the details on your dashboard.", 
                savedProgram.getTitle());
                
        eventPublisher.publishEvent(new GlobalActivityEvent(
                currentUserId,
                savedProgram.getProgramId(),
                "New Tourism Program Launched!",
                message,
                NotificationCategory.EVENT // Or NotificationCategory.PROGRAM
        ));

        return mapToProgramResponse(savedProgram);
    }
    @Override
    @Transactional
    public ProgramResponse updateProgram(Long programId, ProgramRequest request) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND));

        mapRequestToEntity(request, program);
        
        if (request.getHeritageSiteIds() != null) {
            List<HeritageSite> sites = siteRepository.findAllById(request.getHeritageSiteIds());
            program.setHeritageSites(sites);
        }

        TourismProgram updatedProgram = programRepository.save(program);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACT_UPDATE, RES_PROGRAM + "_" + programId, STATUS_SUCCESS);

        return mapToProgramResponse(updatedProgram);
    }

    @Override
    @Transactional
    public ProgramResponse updateProgramStatus(Long programId, String status) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND));
        
        program.setStatus(status.toUpperCase());
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACT_STATUS_CHANGE, RES_PROGRAM + "_" + programId, STATUS_SUCCESS);
        return mapToProgramResponse(programRepository.save(program));
    }

    @Override
    @Transactional
    public void deleteProgram(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND);
        }
        programRepository.deleteById(programId);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACT_DELETE, RES_PROGRAM + "_" + programId, STATUS_SUCCESS);
    }

    @Override
    public ProgramResponse getProgramById(Long programId) {
        return programRepository.findById(programId)
                .map(this::mapToProgramResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND));
    }

    @Override
    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll().stream().map(this::mapToProgramResponse).toList();
    }

    @Override
    public Page<ProgramResponse> getProgramsPaged(int page, int size) {
        return programRepository.findAll(PageRequest.of(page, size)).map(this::mapToProgramResponse);
    }

    // --- Resource Management ---

    @Override
    @Transactional
    public ResourceResponse allocateResourceToProgram(Long programId, ResourceRequest request) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND));

        Resource resource = new Resource();
        resource.setProgram(program);
        resource.setType(request.getType().toUpperCase());
        resource.setQuantity(request.getQuantity());
        resource.setStatus("ALLOCATED");

        Resource savedResource = resourceRepository.save(resource);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACT_ALLOCATE, RES_RESOURCE, STATUS_SUCCESS);
        return mapToResourceResponse(savedResource);
    }

    @Override
    public List<ResourceResponse> getResourcesByProgram(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND);
        }
        return resourceRepository.findByProgram_ProgramId(programId).stream()
                .map(this::mapToResourceResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResourceResponse updateResourceStatus(Long resourceId, String status) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND));
        
        resource.setStatus(status.toUpperCase());
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACT_STATUS_CHANGE, RES_RESOURCE + "_" + resourceId, STATUS_SUCCESS);
        return mapToResourceResponse(resourceRepository.save(resource));
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND));
                
        resourceRepository.delete(resource);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACT_DELETE, RES_RESOURCE + "_" + resourceId, STATUS_SUCCESS);
    }

    @Override
    public Map<String, Object> getBudgetReport(Long programId) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PROGRAM_NOT_FOUND));
        
        List<Resource> resources = resourceRepository.findByProgram_ProgramId(programId);
        
        double totalSpent = resources.stream()
                .filter(r -> "FUNDS".equalsIgnoreCase(r.getType()))
                .mapToDouble(Resource::getQuantity).sum();
                
        double budget = program.getBudget() != null ? program.getBudget() : 0.0;
        return Map.of(
            "title", program.getTitle(), 
            "budget", budget, 
            "spent", totalSpent, 
            "remaining", budget - totalSpent
        );
    }

    // --- Helpers ---

    private void mapRequestToEntity(ProgramRequest request, TourismProgram entity) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setBudget(request.getBudget());
    }

    private ProgramResponse mapToProgramResponse(TourismProgram program) {
        ProgramResponse res = new ProgramResponse();
        res.setProgramId(program.getProgramId());
        res.setTitle(program.getTitle());
        res.setDescription(program.getDescription());
        res.setStartDate(program.getStartDate());
        res.setEndDate(program.getEndDate());
        res.setBudget(program.getBudget());
        res.setStatus(program.getStatus());
        if (program.getHeritageSites() != null) {
            res.setHeritageSiteIds(program.getHeritageSites().stream().map(HeritageSite::getSiteId).toList());
        }
        return res;
    }

    private ResourceResponse mapToResourceResponse(Resource resource) {
        ResourceResponse res = new ResourceResponse();
        res.setResourceId(resource.getResourceId());
        res.setProgramId(resource.getProgram().getProgramId());
        res.setType(resource.getType());
        res.setQuantity(resource.getQuantity());
        res.setStatus(resource.getStatus());
        return res;
    }
}