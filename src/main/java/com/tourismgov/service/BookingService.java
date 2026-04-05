package com.tourismgov.service;

import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(Long eventId, BookingRequest request);
    BookingResponse getBookingById(Long bookingId);
    BookingResponse updateBookingStatus(Long bookingId, BookingRequest request);
    
    List<BookingResponse> getBookingsByEvent(Long eventId);
    List<BookingResponse> getBookingsByTourist(Long touristId);
    
    Page<BookingResponse> getAllBookingsPaged(String status, int page, int size);
    Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size);
}