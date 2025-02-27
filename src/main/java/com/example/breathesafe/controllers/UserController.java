package com.example.breathesafe.controllers;

import com.example.breathesafe.entities.User;
import com.example.breathesafe.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User userRequest) {
        // userRequest contains phoneNumber, name, and a raw PIN (to be hashed in service)
        return userService.createUser(userRequest);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
