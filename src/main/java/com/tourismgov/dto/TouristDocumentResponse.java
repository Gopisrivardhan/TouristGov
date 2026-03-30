package com.tourismgov.dto;

import java.time.LocalDateTime;

import com.tourismgov.enums.VerificationStatus;
import com.tourismgov.model.TouristDocument;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristDocumentResponse {
    private Long documentId;
    private String docType;
    private String fileUri;
    private LocalDateTime uploadedDate;
    private VerificationStatus verificationStatus;

    public static TouristDocumentResponse fromEntity(TouristDocument doc) {
        TouristDocumentResponse response = new TouristDocumentResponse();
        response.setDocumentId(doc.getDocumentId());
        response.setDocType(doc.getDocType());
        response.setFileUri(doc.getFileUri());
        response.setUploadedDate(doc.getUploadedDate());
        response.setVerificationStatus(doc.getVerificationStatus());
        return response;
    }
}
