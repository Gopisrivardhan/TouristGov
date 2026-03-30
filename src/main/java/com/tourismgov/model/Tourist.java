package com.tourismgov.model;

import java.time.LocalDate;
import java.util.List;
import com.tourismgov.enums.Gender;
import com.tourismgov.enums.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tourists")
@Getter
@Setter
@NoArgsConstructor
public class Tourist extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tourist_id")
    private Long touristId;

    @Column(nullable = false)
    private String name;

    private LocalDate dob;
    
    @Column(length = 20)
    private Gender gender;
    
    private String address;
    private String contactInfo;
    
    @Column(nullable = false, length = 50)
    private Status status=Status.ACTIVE;

    // One Tourist can have many Bookings
    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TouristDocument> documents;
 
}