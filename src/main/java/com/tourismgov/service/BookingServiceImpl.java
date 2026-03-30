package com.tourismgov.service;

import com.tourismgov.dto.BookingRequest;
import com.tourismgov.dto.BookingResponse;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.model.Booking;
import com.tourismgov.model.Event;
import com.tourismgov.model.Tourist;
import com.tourismgov.repository.BookingRepository;
import com.tourismgov.repository.EventRepository;
import com.tourismgov.repository.TouristRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.util.List;
import com.tourismgov.autosender.ActivityEvent;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final TouristRepository touristRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponse createBooking(Long eventId, BookingRequest request) {
        log.info("Attempting booking for Event ID: {} by Tourist ID: {}", eventId, request.getTouristId());

        // 1. Fetch the Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
                
        // 2. Fetch the Tourist
        Tourist tourist = touristRepository.findById(request.getTouristId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found"));

        // 3. STRICT VERIFICATION CHECK
        // Assuming your Tourist model has a 'status' or 'isVerified' field
        // If it's an Enum (e.g., TouristStatus.VERIFIED), check against that.
        if (!"VERIFIED".equalsIgnoreCase(tourist.getStatus().toString())) {
            log.warn("Booking Rejected: Tourist {} is not verified.", tourist.getTouristId());
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Tourist cannot be booked without document verification. Please upload and verify your documents first."
            );
        }

        // 4. Proceed with Booking only if verified
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setTourist(tourist);
        booking.setDate(LocalDateTime.now());
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);
        
        // 5. Fire Notification
        eventPublisher.publishEvent(new ActivityEvent(
                tourist.getTouristId(),
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

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponse> getBookingsByEvent(Long eventId) {
        // Validate event exists first
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return bookingRepository.findByEvent_EventId(eventId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTourist(Long touristId) {
        // Validate tourist exists first
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