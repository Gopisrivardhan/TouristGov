package com.tourismgov.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.autosender.ActivityEvent;
import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.model.Booking;
import com.tourismgov.model.Event;
import com.tourismgov.model.Tourist;
import com.tourismgov.repository.BookingRepository;
import com.tourismgov.repository.EventRepository;
import com.tourismgov.repository.TouristRepository;
import com.tourismgov.security.SecurityUtils; // INTEGRATED SECURITY

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final String RESOURCE_BOOKING = "BookingService";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String ACTION_BOOKING_CREATE = "BOOKING_CREATE";
    private static final String ACTION_BOOKING_STATUS_UPDATE = "BOOKING_STATUS_UPDATE";

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final TouristRepository touristRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService; 
    @Override
    @Transactional
    public BookingResponse createBooking(Long eventId, BookingRequest request) {
        log.info("Attempting booking for Event ID: {} with Tourist ID: {}", eventId, request.getTouristId());

        // 1. Fetch the Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        // 2. Fetch the Tourist from the Postman JSON Body
        if (request.getTouristId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tourist ID must be provided in the request body.");
        }
        
        Tourist tourist = touristRepository.findById(request.getTouristId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found with ID: " + request.getTouristId()));

        // 3. Check Tourist Status directly
        if ("INACTIVE".equalsIgnoreCase(tourist.getStatus().toString())) {
            log.warn("Booking Rejected: Tourist {} is INACTIVE.", tourist.getTouristId());
            
            // Log the failed attempt under the person making the request
            Long loggedInUserId = SecurityUtils.getCurrentUserId();
            auditLogService.logAction(loggedInUserId, ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_FAILED);
            
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Tourist account is INACTIVE. Please complete document verification to book events."
            );
        }

        // 4. Create the Booking
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setTourist(tourist);
        booking.setDate(LocalDateTime.now());
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);
        
        // 5. Log Success and Publish Event
        Long loggedInUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(loggedInUserId, ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_SUCCESS);
        
        eventPublisher.publishEvent(new ActivityEvent(
                tourist.getUser().getUserId(),
                tourist.getName(),
                savedBooking.getBookingId(),
                "Booking Confirmed",
                "Your booking for " + event.getTitle() + " is confirmed!",
                NotificationCategory.EVENT
        ));

        return mapToResponse(savedBooking);
    }
    
    @Override
    public BookingResponse getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found")); 
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            booking.setStatus(request.getStatus().toUpperCase());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_BOOKING_STATUS_UPDATE, RESOURCE_BOOKING, STATUS_SUCCESS);
        
        // TRIGGER TARGETED NOTIFICATION
        String message = String.format("The status of your booking for %s has been updated to: %s.", 
                booking.getEvent().getTitle(), updatedBooking.getStatus());

        eventPublisher.publishEvent(new ActivityEvent(
                booking.getTourist().getTouristId(),
                booking.getTourist().getName(),
                booking.getBookingId(),
                "Booking Status Update",
                message,
                NotificationCategory.BOOKING 
        ));
        
        return mapToResponse(updatedBooking);
    }

    // ... The rest of your READ methods remain unchanged ...
    @Override
    public List<BookingResponse> getBookingsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return bookingRepository.findByEvent_EventId(eventId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTourist(Long touristId) {
        if (!touristRepository.existsById(touristId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found");
        }
        return bookingRepository.findByTourist_TouristId(touristId).stream().map(this::mapToResponse).toList(); 
    }

    @Override
    public Page<BookingResponse> getAllBookingsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findAll(pageable).map(this::mapToResponse); 
    }

    @Override
    public Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findByEvent_EventId(eventId, pageable).map(this::mapToResponse);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        if (booking.getTourist() != null) {
            response.setTouristId(booking.getTourist().getTouristId());
        }
        if (booking.getEvent() != null) {
            response.setEventId(booking.getEvent().getEventId());
        }
        response.setDate(booking.getDate());
        response.setStatus(booking.getStatus());
        return response;
    }
}