package com.tourismgov.controller;

import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import com.tourismgov.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long eventId,
            @Valid @RequestBody BookingRequest request) {
        return new ResponseEntity<>(bookingService.createBooking(eventId, request), HttpStatus.CREATED);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, request));
    }

    @GetMapping("/events/{eventId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(bookingService.getBookingsByEvent(eventId));
    }

    @GetMapping("/tourists/{touristId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsForTourist(@PathVariable Long touristId) {
        return ResponseEntity.ok(bookingService.getBookingsByTourist(touristId));
    }

    @GetMapping("/bookings/paged")
    public ResponseEntity<Page<BookingResponse>> getAllBookingsPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.getAllBookingsPaged(status, page, size));
    }

    @GetMapping("/events/{eventId}/bookings/paged")
    public ResponseEntity<Page<BookingResponse>> getBookingsForEventPaged(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.getBookingsByEventPaged(eventId, page, size));
    }
}