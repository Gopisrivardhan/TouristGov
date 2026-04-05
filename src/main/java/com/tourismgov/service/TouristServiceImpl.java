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
import com.tourismgov.exception.TouristErrorMessage;
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

    // --- SONARLINT CONSTANT FIXES ---
    private static final String RESOURCE_TOURIST_SERVICE = "TouristService";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String ACTION_TOURIST_REGISTER = "TOURIST_REGISTER";
    private static final String ACTION_TOURIST_UPDATE = "TOURIST_UPDATE";
    private static final String ACTION_TOURIST_DELETE = "TOURIST_DELETE";

    private final TouristRepository touristRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // NEW: Injected the Audit Log Service
    private final AuditLogService auditLogService;

    // ==========================================
    // 1. REGISTER TOURIST
    // ==========================================
    @Override
    @Transactional
    public TouristResponse createTourist(TouristRequest request) {
        if (touristRepository.findByContactInfo(request.getContactInfo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("A tourist already exists with phone: %s", request.getContactInfo()));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("An account with this email already exists: %s", request.getEmail()));
        }

        // Create User (security side)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());                 
        user.setPhone(request.getContactInfo());           
        user.setPassword(passwordEncoder.encode(request.getPassword())); 
        user.setRole("TOURIST");
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        // Create Tourist (profile side)
        Tourist tourist = new Tourist();
        request.apply(tourist);
        validateAdult(tourist);

        tourist.setUser(savedUser);
        tourist.setStatus(Status.INACTIVE);

        tourist = touristRepository.save(tourist);

        // AUDIT LOG: Uses the special transaction method so it saves alongside the new user!
        auditLogService.logActionInCurrentTransaction(savedUser.getUserId(), ACTION_TOURIST_REGISTER, RESOURCE_TOURIST_SERVICE, STATUS_SUCCESS);

        return TouristResponse.toResponse(tourist);
    }

    // ==========================================
    // 2. GET TOURIST PROFILE
    // ==========================================
    @Override
    public TouristResponse getTouristById(Long touristId) {
        log.info("Fetching tourist with ID: {}", touristId);

        Tourist tourist = touristRepository.findById(touristId).orElseThrow(() -> {
            log.error("Tourist {} not found", touristId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(TouristErrorMessage.ERROR_TOURIST_NOT_FOUND, touristId));
        });

        // Note: We usually DO NOT audit log simple "GET" requests, otherwise the database 
        // will fill up with millions of logs just from people looking at their own profiles.
        return TouristResponse.toResponse(tourist);
    }

    // ==========================================
    // 3. UPDATE TOURIST
    // ==========================================
    @Override
    @Transactional
    public TouristResponse updateTourist(Long touristId, TouristUpdateRequest request) {
        Tourist tourist = touristRepository.findById(touristId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found"));

        User user = tourist.getUser();
        if (user != null) {
            // Only update the fields that are allowed to change
            user.setName(request.getName());
            user.setPhone(request.getContactInfo()); 
        }

        // Apply changes to the tourist entity
        request.apply(tourist);
        validateAdult(tourist);
        
        Tourist updatedTourist = touristRepository.save(tourist);

        // AUDIT LOG: Successfully updated profile
        if (user != null) {
            auditLogService.logAction(user.getUserId(), ACTION_TOURIST_UPDATE, RESOURCE_TOURIST_SERVICE, STATUS_SUCCESS);
        }

        return TouristResponse.toResponse(updatedTourist);
    }
    // ==========================================
    // 4. DELETE TOURIST
    // ==========================================
    @Override
    @Transactional
    public void deleteTourist(Long touristId) {
        log.info("Attempting to delete tourist with ID: {}", touristId);

        Tourist tourist = touristRepository.findById(touristId).orElseThrow(() -> {
            log.error("Delete failed: Tourist {} not found", touristId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(TouristErrorMessage.ERROR_TOURIST_NOT_FOUND, touristId));
        });

        User user = tourist.getUser();

        // 1. Delete the Tourist record first
        touristRepository.delete(tourist);
        log.info("Tourist {} deleted successfully", touristId);

        if (user != null) {
            Long userId = user.getUserId();

            // 3. Now it is completely safe to delete the User record
            userRepository.delete(user);
            log.info("Linked User {} deleted successfully", userId);
            
            // 4. (Optional) Log the deletion. 
            // Note: You must pass 'null' for the user ID here because the user no longer exists!
            auditLogService.logAction(null, ACTION_TOURIST_DELETE, RESOURCE_TOURIST_SERVICE, STATUS_SUCCESS);
        }
    }
    // ==========================================
    // 5. GET ALL (ADMIN)
    // ==========================================
    @Override
    public Page<TouristSummaryResponse> getTouristSummaries(Pageable pageable) {
        log.info("Fetching tourist summaries with pagination: page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Tourist> page = touristRepository.findAll(pageable);
        return page.map(t -> new TouristSummaryResponse(t.getTouristId(), t.getName(), t.getStatus()));
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private void validateAdult(Tourist tourist) {
        if (Period.between(tourist.getDob(), LocalDate.now()).getYears() < 18) {
            log.error("Tourist {} is under 18 years old", tourist.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TouristErrorMessage.ERROR_UNDERAGE_TOURIST);
        }
    }
}