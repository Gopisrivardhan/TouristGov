package com.tourismgov.dto;

import java.time.LocalDate;

import com.tourismgov.enums.Gender;
import com.tourismgov.model.Tourist;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristRequest {
	@NotBlank(message = "Name is required")
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces")
	private String name;

	@NotNull(message = "Date of Birth is required")
	private LocalDate dob;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false, columnDefinition = "ENUM('FEMALE', 'MALE', 'OTHER')")
	private Gender gender;
	@Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
	private String address;

	
	@NotBlank(message = "Contact info is required")
	@Email(message = "Invalid email format")
	private String contactInfo;
	
	@NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;


	public void apply(Tourist tourist) {
		tourist.setName(this.name);
		tourist.setDob(this.dob);
		tourist.setGender(this.gender);
		tourist.setAddress(this.address);
		tourist.setContactInfo(this.contactInfo);
		tourist.setPassword(this.password);
	}
}