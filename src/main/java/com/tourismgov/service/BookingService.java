package com.tourismgov.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;

public interface BookingService {

    // Creates a new booking for a specific event.
    BookingResponse createBooking(Long eventId, BookingRequest request);

    // Retrieves a specific booking by its ID.
    BookingResponse getBookingById(Long bookingId);

    // Updates the status of an existing booking (e.g., CONFIRMED, CANCELLED).
    BookingResponse updateBookingStatus(Long bookingId, BookingRequest request);

    // Retrieves a list of all bookings associated with a specific event.
    List<BookingResponse> getBookingsByEvent(Long eventId);

    // Retrieves a list of all bookings made by a specific tourist.
    List<BookingResponse> getBookingsByTourist(Long touristId);

    // Retrieves a paginated list of all bookings, optionally filtered by status.
    Page<BookingResponse> getAllBookingsPaged(String status, int page, int size);

    // Retrieves a paginated list of bookings for a specific event.
    Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size);
}