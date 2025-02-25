package com.example.lockersystem.controllers;

import com.example.lockersystem.entities.User;
import com.example.lockersystem.services.UserService;
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
