package com.tourismgov.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.dto.TouristRequest;
import com.tourismgov.dto.TouristResponse;
import com.tourismgov.dto.TouristSummaryResponse;
import com.tourismgov.dto.TouristUpdateRequest;
import com.tourismgov.enums.Status;
import com.tourismgov.exception.ErrorMessages;
import com.tourismgov.model.Tourist;
import com.tourismgov.model.User;
import com.tourismgov.repository.TouristRepository;
import com.tourismgov.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TouristServiceImpl implements TouristService {

    private static final String RESOURCE_TOURIST_SERVICE = "TouristService";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String ACTION_TOURIST_REGISTER = "TOURIST_REGISTER";
    private static final String ACTION_TOURIST_UPDATE = "TOURIST_UPDATE";
    private static final String ACTION_TOURIST_DELETE = "TOURIST_DELETE";

    private final TouristRepository touristRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public TouristResponse createTourist(TouristRequest request) {
        log.info("Attempting to register new tourist with email: {}", request.getEmail());

        if (touristRepository.findByContactInfo(request.getContactInfo()).isPresent()) {
            log.warn("Registration failed: Tourist already exists with contact info: {}", request.getContactInfo());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("A tourist already exists with phone: %s", request.getContactInfo()));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Account already exists with email: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("An account with this email already exists: %s", request.getEmail()));
        }

        // Validate business rules before initiating database saves
        Tourist tourist = new Tourist();
        request.apply(tourist);
        validateAdult(tourist);

        // Provision the security user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getContactInfo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("TOURIST");
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        // Link and save the tourist profile
        tourist.setUser(savedUser);
        tourist.setStatus(Status.INACTIVE);
        tourist = touristRepository.save(tourist);

        log.info("Tourist registered successfully with ID: {}", tourist.getTouristId());
        auditLogService.logActionInCurrentTransaction(savedUser.getUserId(), ACTION_TOURIST_REGISTER, RESOURCE_TOURIST_SERVICE, STATUS_SUCCESS);

        return TouristResponse.toResponse(tourist);
    }

    @Override
    public TouristResponse getTouristById(Long touristId) {
        log.info("Fetching tourist profile for ID: {}", touristId);

        Tourist tourist = touristRepository.findById(touristId).orElseThrow(() -> {
            log.error("Fetch failed: Tourist not found for ID: {}", touristId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(ErrorMessages.ERROR_TOURIST_NOT_FOUND, touristId));
        });

        return TouristResponse.toResponse(tourist);
    }

    @Override
    @Transactional
    public TouristResponse updateTourist(Long touristId, TouristUpdateRequest request) {
        log.info("Attempting to update tourist profile for ID: {}", touristId);

        Tourist tourist = touristRepository.findById(touristId)
            .orElseThrow(() -> {
                log.error("Update failed: Tourist not found for ID: {}", touristId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found");
            });

        // Apply and validate profile changes
        request.apply(tourist);
        validateAdult(tourist);

        // Sync shared data with the underlying User account
        User user = tourist.getUser();
        if (user != null) {
            user.setName(request.getName());
            user.setPhone(request.getContactInfo());
        }

        Tourist updatedTourist = touristRepository.save(tourist);
        log.info("Tourist profile updated successfully for ID: {}", touristId);

        if (user != null) {
            auditLogService.logAction(user.getUserId(), ACTION_TOURIST_UPDATE, RESOURCE_TOURIST_SERVICE, STATUS_SUCCESS);
        }

        return TouristResponse.toResponse(updatedTourist);
    }

    @Override
    @Transactional
    public void deleteTourist(Long touristId) {
        log.info("Attempting to delete tourist with ID: {}", touristId);

        Tourist tourist = touristRepository.findById(touristId).orElseThrow(() -> {
            log.error("Delete failed: Tourist not found for ID: {}", touristId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(ErrorMessages.ERROR_TOURIST_NOT_FOUND, touristId));
        });

        User user = tourist.getUser();

        touristRepository.delete(tourist);
        log.info("Tourist record deleted successfully for ID: {}", touristId);

        if (user != null) {
            Long userId = user.getUserId();
            userRepository.delete(user);
            log.info("Linked User account deleted successfully for User ID: {}", userId);
            
            // Retain the historical ID for audit purposes even after the entity is deleted
            auditLogService.logAction(userId, ACTION_TOURIST_DELETE, RESOURCE_TOURIST_SERVICE, STATUS_SUCCESS);
        }
    }

    @Override
    public Page<TouristSummaryResponse> getTouristSummaries(Pageable pageable) {
        log.info("Fetching paginated tourist summaries: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Tourist> page = touristRepository.findAll(pageable);
        return page.map(t -> new TouristSummaryResponse(t.getTouristId(), t.getName(), t.getStatus()));
    }

    /**
     * Validates that the tourist is at least 18 years old.
     */
    private void validateAdult(Tourist tourist) {
        if (tourist.getDob() != null && Period.between(tourist.getDob(), LocalDate.now()).getYears() < 18) {
            log.warn("Age validation failed: Tourist '{}' is under 18 years old", tourist.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.ERROR_UNDERAGE_TOURIST);
        }
    }
}