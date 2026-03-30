package com.tourismgov.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "preservation_activities")
@Getter
@Setter
@NoArgsConstructor
public class PreservationActivity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "activity_id")
	private Long activityId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private HeritageSite site;

	@Column(name = "assigned_officer_id")
	private Long officerId;

	@Column(name = "activity_description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "activity_date")
	private LocalDate date;

	@Column(name = "activity_status")
	private String status = "IN_PROGRESS";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "officer_id")
	private User officer;
}