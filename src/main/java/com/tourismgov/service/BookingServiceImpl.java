package com.tourismgov.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.autosender.ActivityEvent;
import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import com.tourismgov.enums.BookingStatus; // IMPORT ENUM
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.exception.ErrorMessages;
import com.tourismgov.exception.ResourceNotFoundException;
import com.tourismgov.model.Booking;
import com.tourismgov.model.Event;
import com.tourismgov.model.Tourist;
import com.tourismgov.repository.BookingRepository;
import com.tourismgov.repository.EventRepository;
import com.tourismgov.repository.TouristRepository;
import com.tourismgov.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final String RESOURCE_BOOKING = "BookingService";
    private static final String ENTITY_BOOKING = "Booking";
    private static final String ENTITY_EVENT = "Event";
    private static final String ENTITY_TOURIST = "Tourist";
    
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
        log.info("Creating booking for Event ID: {} for Tourist ID: {}", eventId, request.getTouristId());

        // 1. Business Validation: Existence Checks
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_EVENT, eventId));
        
        Tourist tourist = touristRepository.findById(request.getTouristId())
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_TOURIST, request.getTouristId()));

        // 2. Business Validation: Duplicate Check (Idempotency)
        if (bookingRepository.existsByEvent_EventIdAndTourist_TouristId(eventId, request.getTouristId())) {
            log.warn("Duplicate Booking Attempt: Tourist {} for Event {}", request.getTouristId(), eventId);
            throw new IllegalArgumentException(ErrorMessages.DUPLICATE_BOOKING);
        }

        // 3. Business Validation: Account Status Check
        if ("INACTIVE".equalsIgnoreCase(tourist.getStatus().toString())) {
            auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_FAILED);
            throw new IllegalStateException(ErrorMessages.UNAUTHORIZED_ACTION + ": Tourist account is INACTIVE.");
        }

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setTourist(tourist);
        booking.setDate(LocalDateTime.now());
        
        // Use Enum instead of String
        booking.setStatus(BookingStatus.CONFIRMED.name());

        Booking savedBooking = bookingRepository.save(booking);
        
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_SUCCESS);
        
        // 4. Dispatch Notification
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
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_BOOKING, bookingId));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                // Validate against Enum to prevent bad data
                BookingStatus newStatus = BookingStatus.valueOf(request.getStatus().toUpperCase());
                booking.setStatus(newStatus.name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status. Allowed: PENDING, CONFIRMED, CANCELLED, COMPLETED");
            }
        }

        Booking updatedBooking = bookingRepository.save(booking);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_BOOKING_STATUS_UPDATE, RESOURCE_BOOKING, STATUS_SUCCESS);
        
        String message = String.format("The status of your booking for %s has been updated to: %s.", 
                booking.getEvent().getTitle(), updatedBooking.getStatus());

        eventPublisher.publishEvent(new ActivityEvent(
                booking.getTourist().getUser().getUserId(), 
                booking.getTourist().getName(),
                booking.getBookingId(),
                "Booking Status Update",
                message,
                NotificationCategory.BOOKING 
        ));
        
        return mapToResponse(updatedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_BOOKING, bookingId)); 
    }

    @Override
    public List<BookingResponse> getBookingsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException(ENTITY_EVENT, eventId);
        }
        return bookingRepository.findByEvent_EventId(eventId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTourist(Long touristId) {
        if (!touristRepository.existsById(touristId)) {
            throw new ResourceNotFoundException(ENTITY_TOURIST, touristId);
        }
        return bookingRepository.findByTourist_TouristId(touristId).stream().map(this::mapToResponse).toList(); 
    }

    @Override
    public Page<BookingResponse> getAllBookingsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Note: If you want to filter by status in the query, you'd call a custom repo method here
        return bookingRepository.findAll(pageable).map(this::mapToResponse); 
    }

    @Override
    public Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException(ENTITY_EVENT, eventId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findByEvent_EventId(eventId, pageable).map(this::mapToResponse);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        if (booking.getTourist() != null) response.setTouristId(booking.getTourist().getTouristId());
        if (booking.getEvent() != null) response.setEventId(booking.getEvent().getEventId());
        response.setDate(booking.getDate());
        response.setStatus(booking.getStatus());
        return response;
    }
}