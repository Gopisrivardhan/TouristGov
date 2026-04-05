package com.tourismgov.service;

import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse getEventById(Long eventId);
    List<EventResponse> getAllEvents();
    List<EventResponse> getEventsBySite(Long siteId);
    EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request);
    EventResponse updateEvent(Long eventId, CreateEventRequest request);
    void deleteEvent(Long eventId);
    Page<EventResponse> getEventsPaged(String status, int page, int size);
}