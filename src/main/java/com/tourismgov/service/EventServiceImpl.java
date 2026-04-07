package com.tourismgov.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.autosender.GlobalActivityEvent;
import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import com.tourismgov.enums.EventStatus;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.exception.ResourceNotFoundException;
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
    private static final String ENTITY_NAME = "Event";
    private static final String STATUS_SUCCESS = "SUCCESS";
    
    private static final String ACTION_EVENT_CREATE = "EVENT_CREATE";
    private static final String ACTION_EVENT_UPDATE = "EVENT_UPDATE";
    private static final String ACTION_EVENT_STATUS_UPDATE = "EVENT_STATUS_UPDATE";
    private static final String ACTION_EVENT_DELETE = "EVENT_DELETE";

    private final EventRepository eventRepository;
    private final HeritageSiteRepository siteRepository;
    private final TourismProgramRepository programRepository;
    private final AuditLogService auditLogService; 
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: {}", request.getTitle());

        HeritageSite site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Heritage Site", request.getSiteId()));

        Event event = new Event();
        event.setSite(site);
        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                event.setStatus(EventStatus.valueOf(request.getStatus().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Status. Allowed: SCHEDULED, ACTIVE, COMPLETED, CANCELLED, POSTPONED");
            }
        } else {
            event.setStatus(EventStatus.SCHEDULED.name());
        }

        if (request.getProgramId() != null) {
            TourismProgram program = programRepository.findById(request.getProgramId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tourism Program", request.getProgramId()));
            event.setProgram(program);
        }

        Event saved = eventRepository.save(event);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_EVENT_CREATE, RESOURCE_EVENT, STATUS_SUCCESS);
                
        String message = String.format("A new event '%s' has been scheduled at %s on %s.", 
                saved.getTitle(), saved.getLocation(), saved.getDate().toLocalDate());
                
        eventPublisher.publishEvent(new GlobalActivityEvent(
                currentUserId, saved.getEventId(), "New Event Scheduled!", 
                message, NotificationCategory.EVENT 
        ));

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));
        
        String oldStatus = event.getStatus();
        

        try {
            EventStatus newStatusEnum = EventStatus.valueOf(request.getStatus().toUpperCase());
            event.setStatus(newStatusEnum.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Status. Allowed: SCHEDULED, ACTIVE, COMPLETED, CANCELLED, POSTPONED");
        }

        Event updatedEvent = eventRepository.save(event);
        
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_EVENT_STATUS_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        if (!oldStatus.equals(updatedEvent.getStatus())) {
            String message = String.format("Alert: Event '%s' status changed to %s.", 
                    event.getTitle(), updatedEvent.getStatus());
                    
            eventPublisher.publishEvent(new GlobalActivityEvent(
                    SecurityUtils.getCurrentUserId(), event.getEventId(), "Event Status Update",
                    message, NotificationCategory.ALERT
            ));
        }
        
        return mapToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));

        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());

        if (request.getProgramId() != null) {
            TourismProgram program = programRepository.findById(request.getProgramId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tourism Program", request.getProgramId()));
            event.setProgram(program);
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                event.setStatus(EventStatus.valueOf(request.getStatus().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status provided.");
            }
        }
        
        Event updatedEvent = eventRepository.save(event);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_EVENT_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        return mapToResponse(updatedEvent);
    }
    
    @Override public EventResponse getEventById(Long id) {
        return eventRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
    }

    @Override public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override public List<EventResponse> getEventsBySite(Long siteId) {
        return eventRepository.findBySite_SiteId(siteId).stream().map(this::mapToResponse).toList();
    }

    @Override public List<EventResponse> getEventsByProgram(Long programId) {
        return eventRepository.findByProgram_ProgramId(programId).stream().map(this::mapToResponse).toList();
    }

    @Override @Transactional public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) throw new ResourceNotFoundException(ENTITY_NAME, eventId);
        eventRepository.deleteById(eventId);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_EVENT_DELETE, RESOURCE_EVENT, STATUS_SUCCESS);
    }

    @Override
    public Page<EventResponse> getEventsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isBlank()) {
            return eventRepository.findByStatusIgnoreCase(status, pageable).map(this::mapToResponse);
        }
        return eventRepository.findAll(pageable).map(this::mapToResponse); 
    }
    
    

    // --- Helper Mappers & Standard Queries ---

    private EventResponse mapToResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        if (event.getSite() != null) response.setSiteId(event.getSite().getSiteId());
        response.setTitle(event.getTitle());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        response.setStatus(event.getStatus());
        if (event.getProgram() != null) response.setProgramId(event.getProgram().getProgramId());
        return response;
    }
}