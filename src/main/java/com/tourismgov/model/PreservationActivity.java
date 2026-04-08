package com.tourismgov.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	@Column(name = "activity_description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "activity_date")
	private LocalDateTime date;

	@Column(name = "activity_status")
	private String status = "IN_PROGRESS";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "officer_id", nullable = false)
	@JsonIgnoreProperties({"auditLogs", "password", "email"})
	private User officer;
}