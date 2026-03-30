package com.tourismgov.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.dto.DocumentUploadRequest;
import com.tourismgov.dto.DocumentVerifyRequest;
import com.tourismgov.dto.TouristDocumentResponse;
import com.tourismgov.enums.VerificationStatus;
import com.tourismgov.model.Tourist;
import com.tourismgov.model.TouristDocument;
import com.tourismgov.repository.TouristDocumentRepository;
import com.tourismgov.repository.TouristRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TouristDocumentServiceImpl implements TouristDocumentService {

    private final TouristDocumentRepository documentRepository;
    private final TouristRepository touristRepository;

    public TouristDocumentServiceImpl(TouristDocumentRepository documentRepository, TouristRepository touristRepository) {
        this.documentRepository = documentRepository;
        this.touristRepository = touristRepository;
    }

    @Override
    public TouristDocumentResponse uploadDocument(DocumentUploadRequest dto, Long touristId) {
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found"));

        String storedFileUri;

        try {
            if (dto.getFile() != null && !dto.getFile().isEmpty()) {
                String baseUploadDir = new File("uploads").getAbsolutePath();
                String touristFolderPath = Paths.get(baseUploadDir, String.valueOf(touristId)).toString();
                File touristFolder = new File(touristFolderPath);
                
                if (!touristFolder.exists()) {
                    touristFolder.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                Path filePath = Paths.get(touristFolderPath, fileName);

                Files.copy(dto.getFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                storedFileUri = filePath.toString(); 
            } else if (dto.getFileUri() != null && !dto.getFileUri().isBlank()) {
                storedFileUri = dto.getFileUri();
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either file or fileUri must be provided");
            }
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file");
        }

        TouristDocument doc = TouristDocument.builder()
                .tourist(tourist)
                .docType(dto.getDocType())
                .fileUri(storedFileUri)
                .uploadedDate(LocalDateTime.now())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        TouristDocument saved = documentRepository.save(doc);
        return TouristDocumentResponse.fromEntity(saved);
    }

    @Override
    public TouristDocumentResponse verifyDocument(Long touristId, Long documentId, DocumentVerifyRequest request) {
        TouristDocument doc = documentRepository.findByDocumentIdAndTourist_TouristId(documentId, touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found for this tourist"));

        try {
            VerificationStatus newStatus = VerificationStatus.valueOf(request.getStatus().toUpperCase());
            doc.setVerificationStatus(newStatus);
            log.info("Document {} verified as {}", documentId, newStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }

        return TouristDocumentResponse.fromEntity(documentRepository.save(doc));
    }

    @Override
    public TouristDocumentResponse getDocumentMetadata(Long touristId, Long documentId) {
        TouristDocument doc = documentRepository.findByDocumentIdAndTourist_TouristId(documentId, touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        return TouristDocumentResponse.fromEntity(doc);
    }

    @Override
    public void deleteDocument(Long touristId, Long documentId) {
        TouristDocument doc = documentRepository.findByDocumentIdAndTourist_TouristId(documentId, touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        try {
            Path filePath = Paths.get(doc.getFileUri());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Physical file not found or could not be deleted: {}", e.getMessage());
        }

        documentRepository.delete(doc);
    }
}