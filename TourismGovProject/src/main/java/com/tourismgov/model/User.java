package com.tourismgov.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "User")
public class User {



	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "UserID", nullable = false)
	private Long userId;

	@Column(name = "Name", nullable = false, length = 200)
	private String name;

	@Column(name = "Role", length = 100)
	private String role;

	@Column(name = "Email", length = 320, unique = true)
	private String email;

	@Column(name = "Phone", length = 50)
	private String phone;

	@Column(name = "Status", length = 50)
	private String status;

	// Relations
	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<AuditLog> auditLogs = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<PreservationActivity> activities = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Audit> audits = new ArrayList<>();

	@JsonIgnore
	    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
	    private List<Notification> notifications = new ArrayList<>();

	// Getters and setters
	// ...
	// (Generate getters/setters/equals/hashCode/toString as needed)
	// For brevity not included in this snippet
}

