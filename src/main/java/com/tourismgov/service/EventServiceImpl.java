package com.tourismgov.service;

import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import com.tourismgov.model.Event;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.repository.EventRepository;
import com.tourismgov.repository.HeritageSiteRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final HeritageSiteRepository siteRepository;
    private static final String EVENT_NOT_FOUND = "Event not found";

    @Override
    @Transactional // Required for writing data
    public EventResponse createEvent(CreateEventRequest request) {
        HeritageSite site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found"));

        Event event = new Event();
        event.setSite(site);
        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        event.setStatus("SCHEDULED");

        return mapToResponse(eventRepository.save(event));
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
        
        event.setStatus(request.getStatus().toUpperCase());
        return mapToResponse(eventRepository.save(event));
    }
    
    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND));

        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        
        return mapToResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND));
        eventRepository.delete(event);
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
        return response;
    }
}