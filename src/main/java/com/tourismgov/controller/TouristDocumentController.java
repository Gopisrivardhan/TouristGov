package com.tourismgov.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.dto.DocumentUploadRequest;
import com.tourismgov.dto.DocumentVerifyRequest;
import com.tourismgov.dto.TouristDocumentResponse;
import com.tourismgov.service.TouristDocumentService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/touristdoc")
public class TouristDocumentController {
	private final TouristDocumentService touristDocumentService;

	public TouristDocumentController(TouristDocumentService touristDocumentService) {
		this.touristDocumentService = touristDocumentService;
	}

	// Upload Document
	@PostMapping("/{touristId}/documents")
	public ResponseEntity<TouristDocumentResponse> uploadDocument(
	        @PathVariable Long touristId,
	        @ModelAttribute DocumentUploadRequest dto) {

	    TouristDocumentResponse response = touristDocumentService.uploadDocument(dto, touristId);
	    return ResponseEntity.ok(response);
	}


	// Verify Document
	@PutMapping("/{touristId}/documents/{documentId}/verify")
	public TouristDocumentResponse verifyDocument(@PathVariable Long touristId, @PathVariable Long documentId,
			@Valid @RequestBody DocumentVerifyRequest request) {
		return touristDocumentService.verifyDocument(touristId, documentId, request);
	}

	// View Document
	@GetMapping("/{touristId}/documents/{documentId}/view")
	public ResponseEntity<TouristDocumentResponse> viewDocument(@PathVariable Long touristId,
			@PathVariable Long documentId) {
		TouristDocumentResponse response = touristDocumentService.getDocumentMetadata(touristId, documentId);
		return ResponseEntity.ok(response);
	}

	// Delete Document
	@DeleteMapping("/{touristId}/documents/{documentId}")
	public ResponseEntity<String> deleteDocument(@PathVariable Long touristId, @PathVariable Long documentId) {
		touristDocumentService.deleteDocument(touristId, documentId);
		return ResponseEntity.ok("Document deleted successfully");
	}

}
