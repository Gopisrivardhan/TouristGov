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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tourist")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "address", nullable = false)
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;

    // ✅ Phone number only, must start with 6/7/8/9 and be 10 digits
    @Column(name = "contactInfo", unique = true, nullable = false, length = 10)
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone must start with 6, 7, 8, or 9 and be exactly 10 digits")
    private String contactInfo;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Status status = Status.INACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TouristDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
}
