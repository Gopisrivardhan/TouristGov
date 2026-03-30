package com.tourismgov.dto;

import java.time.LocalDate;
import java.util.List;
import com.tourismgov.enums.Gender;
import com.tourismgov.enums.Status;
import com.tourismgov.model.Tourist;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristResponse {
    private Long touristId;
    private String name;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String contactInfo;
    private Status status;
    private List<TouristDocumentResponse> documents;

    public static TouristResponse toResponse(Tourist tourist) {
        TouristResponse response = new TouristResponse();
        response.setTouristId(tourist.getTouristId());
        response.setName(tourist.getName());
        response.setDob(tourist.getDob());
        response.setGender(tourist.getGender());
        response.setAddress(tourist.getAddress());
        response.setContactInfo(tourist.getContactInfo());
        response.setStatus(tourist.getStatus());

        if (tourist.getDocuments() != null) {
            response.setDocuments(
                tourist.getDocuments().stream()
                    .map(TouristDocumentResponse::fromEntity)
                    .toList()
            );
        }
        return response;
    }
}
