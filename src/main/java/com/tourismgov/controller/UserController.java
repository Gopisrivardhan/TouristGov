package com.tourismgov.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.tourismgov.dto.UserResponse;
import com.tourismgov.service.UserService;

@RestController
@RequestMapping("/tourismgov/v1/users")
public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/fetchUsers")
      public List<UserResponse> allUsers() {
          return userService.fetchAllUsers();
      }


}