package com.tourismgov.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.dto.TouristRequest;
import com.tourismgov.dto.TouristResponse;
import com.tourismgov.dto.TouristSummaryResponse;
import com.tourismgov.dto.TouristUpdateRequest;
import com.tourismgov.service.TouristService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tourismgov/v1/tourist")
public class TouristController {

	private final TouristService touristService;

	// Tourist Registration
	@PostMapping("/create")
	public ResponseEntity<TouristResponse> createTourist(@Valid @RequestBody TouristRequest request) {
		log.info("API: create tourist called");
		TouristResponse response = touristService.createTourist(request);
		return ResponseEntity.status(201).body(response);
	}

	// Tourist Profile
	@GetMapping("/{touristId}")
	public ResponseEntity<TouristResponse> getTouristProfile(@PathVariable Long touristId) {
		TouristResponse response = touristService.getTouristById(touristId);
		return ResponseEntity.ok(response);
	}

	// Tourist Profile (Edit)
	@PutMapping("/{touristId}/update")
	public ResponseEntity<TouristResponse> updateTouristProfile(
	        @PathVariable Long touristId,
	        @Valid @RequestBody TouristUpdateRequest request) { // <-- Changed DTO here
	    
	    TouristResponse response = touristService.updateTourist(touristId, request);
	    return ResponseEntity.ok(response);
	}

	// Delete Tourist
	@DeleteMapping("/{touristId}")
	public ResponseEntity<String> deleteTourist(@PathVariable Long touristId) {
		touristService.deleteTourist(touristId);
		return ResponseEntity.ok("Tourist deleted successfully");
	}

	// List of Profiles
	@GetMapping("/admin/all")
	public ResponseEntity<Page<TouristSummaryResponse>> getTouristSummaries(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<TouristSummaryResponse> response = touristService.getTouristSummaries(pageable);
		return ResponseEntity.ok(response);
	}
}
