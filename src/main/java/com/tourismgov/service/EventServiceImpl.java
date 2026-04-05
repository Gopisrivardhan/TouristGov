package com.tourismgov.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.autosender.GlobalActivityEvent;
import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.model.Event;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.TourismProgram;
import com.tourismgov.repository.EventRepository;
import com.tourismgov.repository.HeritageSiteRepository;
import com.tourismgov.repository.TourismProgramRepository;
import com.tourismgov.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String RESOURCE_EVENT = "EventService";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String ACTION_EVENT_CREATE = "EVENT_CREATE";
    private static final String ACTION_EVENT_UPDATE = "EVENT_UPDATE";
    private static final String ACTION_EVENT_STATUS_UPDATE = "EVENT_STATUS_UPDATE";
    private static final String ACTION_EVENT_DELETE = "EVENT_DELETE";
    private static final String EVENT_NOT_FOUND = "Event not found";

    private final EventRepository eventRepository;
    private final HeritageSiteRepository siteRepository;
    private final AuditLogService auditLogService; 
    private final TourismProgramRepository programRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        // 1. Fetch the Site (Mandatory)
        HeritageSite site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found"));

        Event event = new Event();
        event.setSite(site);
        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        
        // Status handling
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            event.setStatus(request.getStatus().toUpperCase());
        } else {
            event.setStatus("SCHEDULED");
        }

        // 2. Fetch the Program (Optional but must be valid if provided)
        if (request.getProgramId() != null) {
            TourismProgram program = programRepository.findById(request.getProgramId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Program not found with ID: " + request.getProgramId()));
            event.setProgram(program);
        }

        Event saved = eventRepository.save(event);
        
        // 3. Log Audit
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_EVENT_CREATE, RESOURCE_EVENT, STATUS_SUCCESS);
                
        // 4. TRIGGER GLOBAL NOTIFICATION
        String message = String.format("A new event '%s' has been scheduled at %s on %s. Book your tickets now!", 
                saved.getTitle(), saved.getLocation(), saved.getDate().toLocalDate());
                
        eventPublisher.publishEvent(new GlobalActivityEvent(
                currentUserId, 
                saved.getEventId(), 
                "New Tourism Event Scheduled!", 
                message, 
                NotificationCategory.EVENT 
        ));

        return mapToResponse(saved);
    }

    @Override
    public EventResponse getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND)); 
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<EventResponse> getEventsBySite(Long siteId) {
        return eventRepository.findBySite_SiteId(siteId).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND));
        
        String oldStatus = event.getStatus();
        String newStatus = request.getStatus().toUpperCase();
        
        event.setStatus(newStatus);
        Event updatedEvent = eventRepository.save(event);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_EVENT_STATUS_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        // TRIGGER GLOBAL NOTIFICATION (Only if status actually changed)
        if (!oldStatus.equals(newStatus)) {
            String message = String.format("Important Alert: The status of the event '%s' scheduled for %s has been changed to %s.", 
                    event.getTitle(), event.getDate().toLocalDate(), newStatus);
                    
            eventPublisher.publishEvent(new GlobalActivityEvent(
                    currentUserId,
                    event.getEventId(),
                    "Event Status Changed",
                    message,
                    NotificationCategory.ALERT // High priority category
            ));
        }
        
        return mapToResponse(updatedEvent);
    }
    
    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND));

        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            event.setStatus(request.getStatus().toUpperCase());
        }
        
        Event updatedEvent = eventRepository.save(event);
        
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_EVENT_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        return mapToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND));
        eventRepository.delete(event);
        
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_EVENT_DELETE, RESOURCE_EVENT, STATUS_SUCCESS);
    }

    @Override
    public Page<EventResponse> getEventsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findAll(pageable).map(this::mapToResponse); 
    }

    private EventResponse mapToResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        
        if (event.getSite() != null) {
            response.setSiteId(event.getSite().getSiteId());
        }
        
        response.setTitle(event.getTitle());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        response.setStatus(event.getStatus());
        
        // CORRECTED: Extract the ID from the Program object to avoid type mismatch
        if (event.getProgram() != null) {
            response.setProgramId(event.getProgram().getProgramId());
        }
        
        return response;
    }
}