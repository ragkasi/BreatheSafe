package com.example.lockersystem.services;

import com.example.lockersystem.entities.User;
import com.example.lockersystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User userRequest) {
        // hash the pin
        String hashedPin = BCrypt.hashpw(userRequest.getHashedPin(), BCrypt.gensalt());
        userRequest.setHashedPin(hashedPin);
        return userRepository.save(userRequest);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
            new RuntimeException("User not found with ID: " + id)
        );
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    // Additional methods (e.g., verify PIN, etc.)
}
