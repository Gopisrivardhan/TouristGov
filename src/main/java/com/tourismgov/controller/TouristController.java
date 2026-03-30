package com.tourismgov.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.dto.TouristRequest;
import com.tourismgov.dto.TouristResponse;
import com.tourismgov.service.TouristService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourist")
public class TouristController {

	private final TouristService touristService;

	public TouristController(TouristService touristService) {
		this.touristService = touristService;
	}

	// Tourist Registration
	@PostMapping("/create")
	public TouristResponse createTourist(@Valid @RequestBody TouristRequest request) {
		log.info("API: create tourist called");
		return touristService.createTourist(request);
	}

	// Tourist Profile
	@GetMapping("/{touristId}")
	public TouristResponse getTouristProfile(@PathVariable Long touristId) {
		return touristService.getTouristById(touristId);
	}

	// Tourist Profile (Edit)
	@PutMapping("/{touristId}/update")
	public TouristResponse updateTouristProfile(@PathVariable Long touristId,
			@Valid @RequestBody TouristRequest request) {
		return touristService.updateTourist(touristId, request);
	}

	// Delete Tourist
	@DeleteMapping("/tourists/{touristId}")
	public ResponseEntity<String> deleteTourist(@PathVariable Long touristId) {
		touristService.deleteTourist(touristId);
		return ResponseEntity.ok("Tourist Deleted Successfully");
	}

	// List of Profile
	@GetMapping("/all")
	public List<TouristResponse> getAllTourists() {
		return touristService.getAllTourists();
	}

}