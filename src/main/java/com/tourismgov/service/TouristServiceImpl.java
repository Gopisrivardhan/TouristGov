package com.tourismgov.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.tourismgov.dto.TouristRequest;
import com.tourismgov.dto.TouristResponse;
import com.tourismgov.enums.Status;
import com.tourismgov.model.Tourist;
import com.tourismgov.repository.TouristRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TouristServiceImpl implements TouristService {

    private final TouristRepository touristRepository;
    private static final String TOURIST_NOT_FOUND = "Tourist not found";

    public TouristServiceImpl(TouristRepository touristRepository) {
        this.touristRepository = touristRepository;
    }

    @Override
    public TouristResponse createTourist(TouristRequest request) {
        log.info("Creating tourist profile");
        Tourist tourist = new Tourist();
        request.apply(tourist);
        tourist.setStatus(Status.ACTIVE); // Default status on creation
        tourist = touristRepository.save(tourist);
        log.info("Tourist created successfully with ID: {}", tourist.getTouristId());
        return TouristResponse.toResponse(tourist);
    }

    @Override
    public TouristResponse getTouristById(Long touristId) {
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TOURIST_NOT_FOUND));
        return TouristResponse.toResponse(tourist);
    }

    @Override
    public TouristResponse updateTourist(Long touristId, TouristRequest request) {
        log.info("Updating tourist with ID: {}", touristId);
        
        // Fixed the lambda syntax error here!
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TOURIST_NOT_FOUND));
        
        request.apply(tourist);
        tourist = touristRepository.save(tourist);
        
        log.info("Tourist updated Successfully: {}", touristId);
        return TouristResponse.toResponse(tourist);
    }
    
    @Override
    public void deleteTourist(Long touristId) {
        log.info("Attempting to delete tourist with ID: {}", touristId);
        
        // Fixed the lambda syntax error here too!
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TOURIST_NOT_FOUND));
                
        touristRepository.delete(tourist);
        log.info("Tourist {} and their records deleted successfully", touristId);
    }

    @Override
    public List<TouristResponse> getAllTourists() {
        return touristRepository.findAll().stream()
                .map(TouristResponse::toResponse)
                .toList();
    }
}