package com.tourismgov.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.dto.DocumentUploadRequest;
import com.tourismgov.dto.DocumentVerifyRequest;
import com.tourismgov.dto.TouristDocumentResponse;
import com.tourismgov.enums.Status;
import com.tourismgov.enums.VerificationStatus;
import com.tourismgov.exception.ErrorMessages;
import com.tourismgov.model.Tourist;
import com.tourismgov.model.TouristDocument;
import com.tourismgov.repository.TouristDocumentRepository;
import com.tourismgov.repository.TouristRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristDocumentServiceImpl implements TouristDocumentService {

	private final TouristDocumentRepository documentRepository;
	private final TouristRepository touristRepository;

	@Override
	@Transactional
	public TouristDocumentResponse uploadDocument(Long touristId, DocumentUploadRequest request) {
		log.info("Uploading document for touristId={} with docType={}", touristId, request.getDocType());

		// Validate tourist
		Tourist tourist = touristRepository.findById(touristId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Tourist not found with id %d", touristId)));

		// Prevent duplicate docType per tourist
		boolean exists = tourist.getDocuments().stream()
				.anyMatch(d -> d.getDocType().equalsIgnoreCase(request.getDocType()));
		if (exists) {
			log.warn("Duplicate document type {} detected for tourist {}", request.getDocType(), touristId);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Tourist already has a document of type %s", request.getDocType()));
		}

		String storedFileUri;
		try {
			if (request.getFile() != null && !request.getFile().isEmpty()) {
				// Handle physical file upload
				Path filePath = Paths.get("uploads", String.valueOf(touristId),
						System.currentTimeMillis() + "_" + request.getFile().getOriginalFilename());
				Files.createDirectories(filePath.getParent());
				Files.copy(request.getFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				storedFileUri = filePath.toUri().toString(); // file:// URI
				log.info("File uploaded successfully for tourist {} at {}", touristId, storedFileUri);
			} else if (request.getFileUri() != null && !request.getFileUri().isBlank()) {
				String uri = request.getFileUri().trim();
				if (uri.startsWith("http://") || uri.startsWith("https://")) {
					// Remote URL → store directly
					storedFileUri = uri;
					log.info("Remote file URI stored for tourist {}: {}", touristId, storedFileUri);
				} else {
					// Treat as local path string (already on server)
					Path filePath = Paths.get("uploads", String.valueOf(touristId),
							System.currentTimeMillis() + "_" + Paths.get(uri).getFileName());
					Files.createDirectories(filePath.getParent());
					Files.copy(Paths.get(uri), filePath, StandardCopyOption.REPLACE_EXISTING);
					storedFileUri = filePath.toUri().toString();
					log.info("Local file copied for tourist {} at {}", touristId, storedFileUri);
				}
			} else {
				log.warn("Upload failed: Missing file or fileUri for tourist {}", touristId);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing file or fileUri");
			}
		} catch (IOException e) {
			log.warn("File save failed for tourist {}: {}", touristId, e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File save failed: " + e.getMessage());
		}

		// Build and save document
		TouristDocument doc = TouristDocument.builder().tourist(tourist).docType(request.getDocType())
				.fileUri(storedFileUri).uploadedDate(LocalDateTime.now()).verificationStatus(VerificationStatus.PENDING)
				.build();

		TouristDocument saved = documentRepository.save(doc);
		log.info("Document {} uploaded successfully for tourist {}", saved.getDocumentId(), touristId);
		return TouristDocumentResponse.fromEntity(saved);
	}

	@Override
	@Transactional
	public TouristDocumentResponse verifyDocument(Long touristId, Long documentId, DocumentVerifyRequest request) {
		log.info("Verifying document {} for tourist {}", documentId, touristId);
		TouristDocument doc = getTouristDocumentOrThrow(touristId, documentId);

		VerificationStatus newStatus;
		try {
			newStatus = VerificationStatus.valueOf(request.getStatus().toUpperCase());
		} catch (IllegalArgumentException e) {
			log.warn("Invalid verification status '{}' for document {} of tourist {}", request.getStatus(), documentId,
					touristId);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format(ErrorMessages.ERROR_INVALID_VERIFICATION_STATUS, request.getStatus()));
		}

		doc.setVerificationStatus(newStatus);
		documentRepository.save(doc);
		log.info("Document {} for tourist {} updated to status {}", documentId, touristId, newStatus);

		syncTouristStatus(doc.getTourist());
		return TouristDocumentResponse.fromEntity(doc);
	}

	@Override
	public TouristDocumentResponse getDocumentMetadata(Long touristId, Long documentId) {
		log.info("Fetching metadata for document {} of tourist {}", documentId, touristId);
		TouristDocument doc = getTouristDocumentOrThrow(touristId, documentId);
		log.info("Metadata fetched successfully for document {} of tourist {}", documentId, touristId);
		return TouristDocumentResponse.fromEntity(doc);
	}

	@Override
	@Transactional
	public void deleteDocument(Long touristId, Long documentId) {
		log.info("Deleting document {} for tourist {}", documentId, touristId);
		TouristDocument doc = getTouristDocumentOrThrow(touristId, documentId);
		Tourist tourist = doc.getTourist();

		try {
			String fileUri = doc.getFileUri();
			if (!(fileUri.startsWith("http://") || fileUri.startsWith("https://"))) {
				Path filePath = Paths.get(URI.create(fileUri));
				Files.deleteIfExists(filePath);
				log.info("File deleted for document {} of tourist {}", documentId, touristId);
			}
		} catch (Exception e) {
			log.error("Failed to delete file for document {}: {}", documentId, e.getMessage());
		}

		// Remove from collection first
		tourist.getDocuments().remove(doc);

		// Delete from repository
		documentRepository.delete(doc);
		log.info("Document {} deleted successfully for tourist {}", documentId, touristId);
		// Sync status safely
		syncTouristStatus(tourist);
	}

	private TouristDocument getTouristDocumentOrThrow(Long touristId, Long documentId) {
		touristRepository.findById(touristId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
				String.format(ErrorMessages.ERROR_TOURIST_NOT_FOUND, touristId)));

		return documentRepository.findByDocumentIdAndTourist_TouristId(documentId, touristId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(ErrorMessages.ERROR_DOCUMENT_NOT_FOUND, documentId, touristId)));
	}

	private void syncTouristStatus(Tourist tourist) {
		boolean hasVerified = tourist.getDocuments().stream()
				.allMatch(d -> d.getVerificationStatus() == VerificationStatus.VERIFIED);

		Status newStatus = hasVerified ? Status.ACTIVE : Status.INACTIVE;
		tourist.setStatus(newStatus);
		touristRepository.save(tourist);
	}
}
