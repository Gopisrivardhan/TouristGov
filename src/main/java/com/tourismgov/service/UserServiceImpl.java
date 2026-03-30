package com.tourismgov.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tourismgov.dto.UserRequest;
import com.tourismgov.dto.UserResponse;
import com.tourismgov.model.User;
import com.tourismgov.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- NEW HELPER METHOD ---
    // Private methods don't need @Transactional and avoid the proxy bypass warning
    private User mapToEntityAndSave(UserRequest request) {
        User user = new User();
        if (request.getUserId() != null) {
            user.setUserId(request.getUserId());
        }
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); 
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User create(UserRequest request) {
        return mapToEntityAndSave(request); // Calls the private helper
    }

    @Override
    @Transactional
    public List<User> createAll(List<UserRequest> requests) {
        List<User> toSave = new ArrayList<>();
        for (UserRequest r : requests) {
            User user = new User();
            if (r.getUserId() != null) {
                user.setUserId(r.getUserId());
            }
            user.setName(r.getName());
            user.setRole(r.getRole());
            user.setEmail(r.getEmail());
            user.setPassword(passwordEncoder.encode(r.getPassword())); 
            user.setPhone(r.getPhone());
            user.setStatus(r.getStatus());
            toSave.add(user);
        }
        return userRepository.saveAll(toSave);
    }
    
    @Override
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        User user = mapToEntityAndSave(request); // Calls the private helper safely
        return toResponse(user);
    }

    @Override
    public UserResponse toResponse(User user) {
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