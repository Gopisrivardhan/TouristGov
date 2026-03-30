package com.tourismgov.dto;

import lombok.Data;

@Data
public class UserResponse {

	private Long userId;
	private String name;
	private String role;
	private String email;
	private String phone;
	private String status;
}