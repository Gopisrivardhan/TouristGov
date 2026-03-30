package com.tourismgov.service;

import com.tourismgov.dto.AuthRequest;
import com.tourismgov.dto.AuthResponse;
import com.tourismgov.dto.UserRequest;
import com.tourismgov.dto.UserResponse;

public interface AuthService {
    UserResponse registerUser(UserRequest request);
    AuthResponse loginUser(AuthRequest request);
}