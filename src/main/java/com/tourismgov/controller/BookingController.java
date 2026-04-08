package com.tourismgov.controller;

import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import com.tourismgov.service.BookingService;
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
@RequestMapping("/tourismgov/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Creates a new booking for a specific event
    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long eventId,
            @Valid @RequestBody BookingRequest request) {
        log.info("REST request to create booking for Event ID: {} with Tourist ID: {}", eventId, request.getTouristId());
        return new ResponseEntity<>(bookingService.createBooking(eventId, request), HttpStatus.CREATED);
    }

    // Retrieves a specific booking by its ID
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        log.info("REST request to fetch Booking ID: {}", bookingId);
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    // Updates the status of an existing booking (e.g., CONFIRMED to CANCELLED)
    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingRequest request) {
        log.info("REST request to update status for Booking ID: {}", bookingId);
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, request));
    }

    // Retrieves all bookings associated with a specific event
    @GetMapping("/events/{eventId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsForEvent(@PathVariable Long eventId) {
        log.info("REST request to fetch all bookings for Event ID: {}", eventId);
        return ResponseEntity.ok(bookingService.getBookingsByEvent(eventId));
    }

    // Retrieves all bookings made by a specific tourist
    @GetMapping("/tourists/{touristId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsForTourist(@PathVariable Long touristId) {
        log.info("REST request to fetch all bookings for Tourist ID: {}", touristId);
        return ResponseEntity.ok(bookingService.getBookingsByTourist(touristId));
    }

    // Retrieves a paginated list of all bookings across the system
    @GetMapping("/bookings/paged")
    public ResponseEntity<Page<BookingResponse>> getAllBookingsPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch paged bookings (status: {}, page: {}, size: {})", status, page, size);
        return ResponseEntity.ok(bookingService.getAllBookingsPaged(status, page, size));
    }

    // Retrieves a paginated list of bookings restricted to a specific event
    @GetMapping("/events/{eventId}/bookings/paged")
    public ResponseEntity<Page<BookingResponse>> getBookingsForEventPaged(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch paged bookings for Event ID: {} (page: {}, size: {})", eventId, page, size);
        return ResponseEntity.ok(bookingService.getBookingsByEventPaged(eventId, page, size));
    }
}