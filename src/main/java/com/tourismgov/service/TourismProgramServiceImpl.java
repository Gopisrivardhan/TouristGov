package com.tourismgov.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.autosender.GlobalActivityEvent;
import com.tourismgov.dto.ProgramRequest;
import com.tourismgov.dto.ProgramResponse;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.enums.ProgramStatus;
import com.tourismgov.exception.ResourceNotFoundException;
import com.tourismgov.model.Event;
import com.tourismgov.model.Resource;
import com.tourismgov.model.TourismProgram;
import com.tourismgov.repository.EventRepository;
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

    private static final String ENTITY_NAME = "Tourism Program";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private final TourismProgramRepository programRepository;
    private final EventRepository eventRepository;
    private final ResourceRepository resourceRepository;
    private final AuditLogService auditLogService; 
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        log.info("Creating Tourism Program: {}", request.getTitle());
        
        // VALIDATION: Strict date logic
        validateProgramDates(request.getStartDate(), request.getEndDate(), true);

        TourismProgram program = new TourismProgram();
        mapRequestToEntity(request, program);
        
        // Use Enum instead of String
        program.setStatus(ProgramStatus.PLANNED.name());

        TourismProgram saved = programRepository.save(program);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, "CREATE", ENTITY_NAME, STATUS_SUCCESS);

        eventPublisher.publishEvent(new GlobalActivityEvent(
                currentUserId, saved.getProgramId(), "New Program Launched!",
                "Tourism program '" + saved.getTitle() + "' has been initiated.",
                NotificationCategory.EVENT 
        ));

        return mapToProgramResponse(saved);
    }

    @Override
    @Transactional
    public ProgramResponse updateProgramStatus(Long id, String statusString) {
        TourismProgram p = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
        
        try {
            // Validation: Check if the string matches our Enum
            ProgramStatus status = ProgramStatus.valueOf(statusString.toUpperCase());
            p.setStatus(status.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status provided. Allowed: PLANNED, ACTIVE, COMPLETED, CANCELLED");
        }

        TourismProgram updated = programRepository.save(p);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), "STATUS_UPDATE", ENTITY_NAME, STATUS_SUCCESS);
        
        return mapToProgramResponse(updated);
    }

    @Override
    public Map<String, Object> getBudgetReport(Long programId) {
        log.info("Generating budget report for Program ID: {}", programId);
        
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));

        List<Resource> resources = resourceRepository.findByProgram_ProgramId(programId);
        double totalBudget = (program.getBudget() != null) ? program.getBudget() : 0.0;
        
        // Business Rule: Only count "FUNDS" that are actually "ALLOCATED"
        double spentFunds = resources.stream()
                .filter(r -> "FUNDS".equalsIgnoreCase(r.getType()) && "ALLOCATED".equalsIgnoreCase(r.getStatus()))
                .mapToDouble(r -> (r.getQuantity() != null) ? r.getQuantity() : 0.0)
                .sum();

        Map<String, Object> report = new HashMap<>();
        report.put("programTitle", program.getTitle());
        report.put("totalBudget", totalBudget);
        report.put("amountSpent", spentFunds);
        report.put("remainingBudget", totalBudget - spentFunds);
        report.put("currentStatus", program.getStatus());
        
        return report;
    }

    /*
     * PRIVATE VALIDATION & MAPPING
     **/

    private void validateProgramDates(LocalDate start, LocalDate end, boolean isNew) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates are required.");
        }
        // Business Rule: Cannot create programs that started in the past
        if (isNew && start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A new program cannot start in the past.");
        }
        // Business Rule: Logic sequence
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date (" + end + ") cannot be before start date (" + start + ").");
        }
    }

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
        
        // Link site IDs via the Event bridge architecture
        List<Event> events = eventRepository.findByProgram_ProgramId(program.getProgramId());
        if (events != null) {
            res.setHeritageSiteIds(events.stream()
                    .map(e -> e.getSite().getSiteId())
                    .distinct()
                    .toList());
        }
        return res;
    }

    // Standard CRUD
    @Override 
    @Transactional
    public ProgramResponse updateProgram(Long programId, ProgramRequest request) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));

        validateProgramDates(request.getStartDate(), request.getEndDate(), false);
        mapRequestToEntity(request, program);
        
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), "UPDATE", ENTITY_NAME, STATUS_SUCCESS);
        return mapToProgramResponse(programRepository.save(program));
    }

    @Override 
    @Transactional
    public void deleteProgram(Long programId) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));
        
        // Professional Clean-up: Unlink events safely
        List<Event> linkedEvents = eventRepository.findByProgram_ProgramId(programId);
        if (!linkedEvents.isEmpty()) {
            linkedEvents.forEach(e -> e.setProgram(null));
            eventRepository.saveAll(linkedEvents);
        }
        
        programRepository.delete(program);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), "DELETE", ENTITY_NAME, STATUS_SUCCESS);
    }

    @Override public ProgramResponse getProgramById(Long id) {
        return programRepository.findById(id).map(this::mapToProgramResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
    }

    @Override public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll().stream().map(this::mapToProgramResponse).toList();
    }

    @Override public Page<ProgramResponse> getProgramsPaged(int page, int size) {
        return programRepository.findAll(PageRequest.of(page, size)).map(this::mapToProgramResponse);
    }
}