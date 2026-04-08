package com.tourismgov.controller;

import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import com.tourismgov.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/events") 
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Creates a new scheduled tourism event
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        log.info("REST request to create Event: '{}' at Site ID: {}", request.getTitle(), request.getSiteId());
        return new ResponseEntity<>(eventService.createEvent(request), HttpStatus.CREATED);
    }

    // Retrieves a specific event by its ID
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId) {
        log.info("REST request to fetch Event ID: {}", eventId);
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    // Retrieves all scheduled events
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.info("REST request to fetch all events");
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // Retrieves all events associated with a specific heritage site
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<EventResponse>> getEventsBySite(@PathVariable Long siteId) {
        log.info("REST request to fetch events for Site ID: {}", siteId);
        return ResponseEntity.ok(eventService.getEventsBySite(siteId));
    }

    // Fully updates the details of an existing event
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventRequest request) {
        log.info("REST request to update Event ID: {}", eventId);
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    // Updates the status of an existing event (e.g., SCHEDULED to CANCELLED)
    @PatchMapping("/{eventId}/status")
    public ResponseEntity<EventResponse> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventStatusRequest request) {
        log.info("REST request to update status for Event ID: {}", eventId);
        return ResponseEntity.ok(eventService.updateEventStatus(eventId, request));
    }

    // Deletes an event from the system
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId) {
        log.info("REST request to delete Event ID: {}", eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok("Event deleted successfully");
    }

    // Retrieves a paginated list of events, optionally filtered by status
    @GetMapping("/paged")
    public ResponseEntity<Page<EventResponse>> getEventsPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch paged events (status: {}, page: {}, size: {})", status, page, size);
        return ResponseEntity.ok(eventService.getEventsPaged(status, page, size));
    }
}