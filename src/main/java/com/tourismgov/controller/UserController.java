package com.tourismgov.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tourismgov.dto.UserResponse;
import com.tourismgov.repository.UserRepository;
import com.tourismgov.service.UserService;

@RestController
@RequestMapping("/tourismgov/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/fetchUsers")
    public List<UserResponse> allUsers() {
        // Reused the toResponse method from your UserService
        return userRepository.findAll().stream().map(userService::toResponse).toList();
    }
}