package com.tourismgov.service;

import com.tourismgov.dto.AuthRequest;
import com.tourismgov.dto.AuthResponse;
import com.tourismgov.dto.PasswordResetRequest;
import com.tourismgov.dto.PasswordUpdateRequest;
import com.tourismgov.dto.UserRequest;
import com.tourismgov.dto.UserResponse;

public interface AuthService {
    UserResponse registerUser(UserRequest request);
    AuthResponse loginUser(AuthRequest request);
    void updatePassword(PasswordUpdateRequest request);
    void resetPassword(PasswordResetRequest request);
}