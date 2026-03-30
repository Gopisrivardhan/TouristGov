package com.tourismgov.service;

import java.util.List;
import com.tourismgov.dto.UserRequest;
import com.tourismgov.dto.UserResponse;
import com.tourismgov.model.User;

public interface UserService {
    User create(UserRequest request);
    List<User> createAll(List<UserRequest> requests);
    UserResponse registerUser(UserRequest request);
    UserResponse toResponse(User user);
}