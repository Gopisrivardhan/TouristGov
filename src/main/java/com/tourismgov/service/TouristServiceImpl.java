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

	private final TouristRepository touristRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

	// Register
    @Transactional
    public TouristResponse createTourist(TouristRequest request) {
        // Check duplicate phone
        if (touristRepository.findByContactInfo(request.getContactInfo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("A tourist already exists with phone: %s", request.getContactInfo()));
        }

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("An account with this email already exists: %s", request.getEmail()));
        }

        // Create User (security side)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());                 // ✅ email stored in User
        user.setPhone(request.getContactInfo());           // ✅ phone stored in User
        user.setPassword(passwordEncoder.encode(request.getPassword())); // ✅ password only in User
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

        return TouristResponse.toResponse(tourist);
    }


	// Profile
	public TouristResponse getTouristById(Long touristId) {
		log.info("Fetching tourist with ID: {}", touristId);

		Tourist tourist = touristRepository.findById(touristId).orElseThrow(() -> {
			log.error("Tourist {} not found", touristId);
			return new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format(TouristErrorMessage.ERROR_TOURIST_NOT_FOUND, touristId));
		});

		log.info("Tourist {} fetched successfully", touristId);
		return TouristResponse.toResponse(tourist);
	}

	// Update Profile
	@Transactional
	public TouristResponse updateTourist(Long touristId, TouristRequest request) {
	    Tourist tourist = touristRepository.findById(touristId)
	        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found"));

	    User user = tourist.getUser();
	    if (user != null) {
	        user.setName(request.getName());
	        user.setEmail(request.getContactInfo());
	        
	        if (request.getPassword() != null && !request.getPassword().isBlank()) {
	            user.setPassword(passwordEncoder.encode(request.getPassword()));
	        }
	    }

	    request.apply(tourist);
	    validateAdult(tourist);
	    return TouristResponse.toResponse(tourist);
	}

	// Delete Tourist
	@Transactional
	public void deleteTourist(Long touristId) {
	    log.info("Attempting to delete tourist with ID: {}", touristId);

	    Tourist tourist = touristRepository.findById(touristId).orElseThrow(() -> {
	        log.error("Delete failed: Tourist {} not found", touristId);
	        return new ResponseStatusException(HttpStatus.NOT_FOUND,
	                String.format(TouristErrorMessage.ERROR_TOURIST_NOT_FOUND, touristId));
	    });

	    // Get linked User
	    User user = tourist.getUser();

	    // First delete Tourist
	    touristRepository.delete(tourist);
	    log.info("Tourist {} deleted successfully", touristId);

	    // Then delete User if exists
	    if (user != null) {
	        userRepository.delete(user);
	        log.info("Linked User {} deleted successfully", user.getUserId());
	    }
	}


	// List of Profiles (Admin)
	public Page<TouristSummaryResponse> getTouristSummaries(Pageable pageable) {
		log.info("Fetching tourist summaries with pagination: page={}, size={}", pageable.getPageNumber(),
				pageable.getPageSize());

		Page<Tourist> page = touristRepository.findAll(pageable);
		log.info("Fetched {} tourist records", page.getTotalElements());

		return page.map(t -> new TouristSummaryResponse(t.getTouristId(), t.getName(), t.getStatus()));
	}

	private void validateAdult(Tourist tourist) {
		if (Period.between(tourist.getDob(), LocalDate.now()).getYears() < 18) {
			log.error("Tourist {} is under 18 years old", tourist.getName());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TouristErrorMessage.ERROR_UNDERAGE_TOURIST);
		}
	}
}
