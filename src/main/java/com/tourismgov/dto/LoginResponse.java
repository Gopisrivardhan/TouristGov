package com.tourismgov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private Long id;
    private String username;
    private String email;
    private String role;

    // Custom constructor for quick token generation
    public LoginResponse(String token, String role, String email) {
        this.token = token;
        this.role = role;
        this.email = email;
        this.type = "Bearer";
    }
}