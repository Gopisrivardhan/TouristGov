package com.tourismgov.service;

import com.tourismgov.dto.UserRequest;
import com.tourismgov.dto.UserResponse;
import com.tourismgov.model.User;

import java.util.List;

public interface UserService {
    User create(UserRequest request);
    List<User> createAll(List<UserRequest> requests);
    List<UserResponse> fetchAllUsers();
    UserResponse registerUser(UserRequest request);
    UserResponse toResponse(User user);
}