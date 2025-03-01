package com.example.breathesafe.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.example.breathesafe.entities.User;
import com.example.breathesafe.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User userRequest) {
        // Hash the PIN if provided; if not, leave it as an empty string.
        if (userRequest.getHashedPin() != null && !userRequest.getHashedPin().isEmpty()) {
            String hashedPin = BCrypt.hashpw(userRequest.getHashedPin(), BCrypt.gensalt());
            userRequest.setHashedPin(hashedPin);
        }
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
    
    public void updateUserPin(User user, String rawPin) {
        String hashedPin = BCrypt.hashpw(rawPin, BCrypt.gensalt());
        user.setHashedPin(hashedPin);
        userRepository.save(user);
    }
    
    public boolean verifyUserPin(User user, String rawPin) {
        if (user.getHashedPin() == null || user.getHashedPin().isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(rawPin, user.getHashedPin());
    }
    
    public void updateUserName(User user, String name) {
        user.setName(name);
        userRepository.save(user);
    }
    
    public void clearUserPin(User user) {
        user.setHashedPin(null);
        userRepository.save(user);
    }
    
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}
