package com.tourismgov.service;

import com.tourismgov.dto.DocumentUploadRequest;
import com.tourismgov.dto.DocumentVerifyRequest;
import com.tourismgov.dto.TouristDocumentResponse;

public interface TouristDocumentService {

    /**
     * Handles physical file storage or URI mapping for tourist documents.
     */
    TouristDocumentResponse uploadDocument(DocumentUploadRequest dto, Long touristId);

    /**
     * Updates the VerificationStatus (PENDING, VERIFIED, REJECTED).
     */
    TouristDocumentResponse verifyDocument(Long touristId, Long documentId, DocumentVerifyRequest request);

    /**
     * Retrieves metadata for a specific document belonging to a tourist.
     */
    TouristDocumentResponse getDocumentMetadata(Long touristId, Long documentId);

    /**
     * Removes the file from the 'uploads' directory and deletes the DB record.
     */
    void deleteDocument(Long touristId, Long documentId);
}