package com.tourismgov.controller;

import com.tourismgov.dto.CreateEventRequest;
import com.tourismgov.dto.EventResponse;
import com.tourismgov.dto.UpdateEventStatusRequest;
import com.tourismgov.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/events") // Standardized to match your /api/v1 architecture
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        log.info("REST request to create Event: {} at Site ID: {}", request.getTitle(), request.getSiteId());
        return new ResponseEntity<>(eventService.createEvent(request), HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<EventResponse>> getEventsBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(eventService.getEventsBySite(siteId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    @PatchMapping("/{eventId}/status")
    public ResponseEntity<EventResponse> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventStatusRequest request) {
        return ResponseEntity.ok(eventService.updateEventStatus(eventId, request));
    }

    //Delete Event Endpoint
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId) {
        log.info("REST request to delete Event ID: {}", eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok("Event deleted successfully");
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<EventResponse>> getEventsPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getEventsPaged(status, page, size));
    }
}