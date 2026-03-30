package com.tourismgov.dto;

import java.time.LocalDate;

import com.tourismgov.enums.Gender;
import com.tourismgov.model.Tourist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristRequest {
	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Date of Birth is required")
	private LocalDate dob;

	private Gender gender;
	private String address;

	@NotBlank(message = "Contact info is required")
	private String contactInfo;

	public void apply(Tourist tourist) {
		tourist.setName(this.name);
		tourist.setDob(this.dob);
		tourist.setGender(this.gender);
		tourist.setAddress(this.address);
		tourist.setContactInfo(this.contactInfo);
	}
}