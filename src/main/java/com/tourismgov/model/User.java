package com.tourismgov.model;

import java.util.List;

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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "email", unique = true, nullable = false, length = 150)
	private String email;

	@Column(name = "role", nullable = false, length = 50)
	private String role; // Tourist/Officer/Admin

	@Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
	private String status = "ACTIVE";

	// One-to-Many: A user can have many audit logs
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<AuditLog> auditLogs;
	
	//newer options
	@Column(name = "password", nullable = false)
    private String password;
	
	@Column(name = "phone", length = 10)
    private String phone;
}