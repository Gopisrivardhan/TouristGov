package com.tourismgov.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tourismgov.enums.Gender;
import com.tourismgov.enums.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tourist")
public class Tourist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long touristId;

	@Column(name = "name", nullable = false, length = 100)
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces")
	private String name;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Column(name = "dob", nullable = false)
	@NotNull(message = "Date of Birth is required")
	private LocalDate dob;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false, columnDefinition = "ENUM('FEMALE', 'MALE', 'OTHER')")
	private Gender gender;

	@Column(name = "address", nullable= false)
	@Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
	private String address;

	@Column(name = "contactInfo", unique = true, nullable = false, length = 100)
	@Email(message = "Invalid email format")
	private String contactInfo;
	
	@NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

	@Column(name = "status", nullable = false, length = 50)
	@Enumerated(EnumType.STRING)
	private Status status = Status.INACTIVE; // ACTIVE, INACTIVE

	@OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TouristDocument> documents = new ArrayList<>();

	// One Tourist can have many Bookings
	@OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Booking> bookings;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
}