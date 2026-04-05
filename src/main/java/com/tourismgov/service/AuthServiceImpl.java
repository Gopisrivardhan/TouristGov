package com.tourismgov.service;

import com.tourismgov.dto.*;
import com.tourismgov.exception.*;
import com.tourismgov.model.User;
import com.tourismgov.repository.UserRepository;
import com.tourismgov.security.JwtUtil;
import com.tourismgov.security.SecurityUtils; // INTEGRATED SECURITY

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j 
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String RESOURCE_AUTH_SERVICE = "AuthService";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String ACTION_USER_REGISTER = "USER_REGISTER";
    private static final String ACTION_USER_LOGIN = "USER_LOGIN";
    private static final String ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private static final String ACTION_RESET_PASSWORD = "RESET_PASSWORD";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService; 

    @Override
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole().toUpperCase());
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        // SecurityUtils is NOT used here because the user is not logged in yet!
        auditLogService.logActionInCurrentTransaction(savedUser.getUserId(), ACTION_USER_REGISTER, RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);

        return mapToUserResponse(savedUser);
    }

    @Override
    public AuthResponse loginUser(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                auditLogService.logAction(user.getUserId(), ACTION_USER_LOGIN, RESOURCE_AUTH_SERVICE, STATUS_FAILED);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
            }

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);

            auditLogService.logAction(user.getUserId(), ACTION_USER_LOGIN, RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);

            return new AuthResponse(jwt, user.getUserId(), user.getRole(), user.getName());

        } catch (AuthenticationException ex) {
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> 
                auditLogService.logAction(user.getUserId(), ACTION_USER_LOGIN, RESOURCE_AUTH_SERVICE, STATUS_FAILED)
            );
            
            log.error("Failed login attempt for email: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }
    
    @Override
    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        // SECURITY CHECK: Ensure the logged-in user matches the user trying to update the password
        Long loggedInUserId = SecurityUtils.getCurrentUserId();
        if (!loggedInUserId.equals(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are only authorized to update your own password.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            auditLogService.logAction(user.getUserId(), ACTION_UPDATE_PASSWORD, RESOURCE_AUTH_SERVICE, STATUS_FAILED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        auditLogService.logAction(user.getUserId(), ACTION_UPDATE_PASSWORD, RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);
    }
    
    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Assuming this is an admin action
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_RESET_PASSWORD, RESOURCE_AUTH_SERVICE, STATUS_SUCCESS);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        return dto;
    }
}