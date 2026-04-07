package com.tourismgov.service;

import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EventService {

    // Creates a new scheduled tourism event (Must link to a Site, optionally to a Program).
    EventResponse createEvent(CreateEventRequest request);

    // Retrieves the details of a specific event by its ID.
    EventResponse getEventById(Long eventId);

    // Retrieves a list of all scheduled events in the system.
    List<EventResponse> getAllEvents();

    // THE BRIDGE: Retrieves all events associated with a specific heritage site.
    List<EventResponse> getEventsBySite(Long siteId);

    // THE BRIDGE: Retrieves all events belonging to a specific tourism program.
    List<EventResponse> getEventsByProgram(Long programId);

    // Updates the status of an existing event (e.g., SCHEDULED, CANCELLED, COMPLETED).
    EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request);

    // Fully updates the details of an existing event.
    EventResponse updateEvent(Long eventId, CreateEventRequest request);

    // Deletes an event from the system.
    void deleteEvent(Long eventId);

    // Retrieves a paginated list of events, optionally filtered by status.
    Page<EventResponse> getEventsPaged(String status, int page, int size);
}