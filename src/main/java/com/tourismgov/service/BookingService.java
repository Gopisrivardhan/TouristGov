package com.tourismgov.service;

import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import org.springframework.data.domain.Page; 


import java.util.List;

public interface BookingService {
    BookingResponse createBooking(Long eventId, BookingRequest request);
    List<BookingResponse> getBookingsByEvent(Long eventId);
    BookingResponse updateBookingStatus(Long bookingId, BookingRequest request);
    List<BookingResponse> getBookingsByTourist(Long touristId);
    BookingResponse getBookingById(Long bookingId);
    
    // Paginated methods
    Page<BookingResponse> getAllBookingsPaged(String status, int page, int size);
    Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size);
}