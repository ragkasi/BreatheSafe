package com.example.breathesafe.services;

import com.example.breathesafe.entities.Locker;
import com.example.breathesafe.entities.User;
import com.example.breathesafe.repositories.LockerRepository;
import com.example.breathesafe.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private UserRepository userRepository;

    public Locker assignLocker(Long lockerId, Long userId) {
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found with ID: " + lockerId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        locker.setAssignedUser(user);
        locker.setLocked(true);
        return lockerRepository.save(locker);
    }

    public Locker unlockLocker(Long lockerId, Long userId) {
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found with ID: " + lockerId));
        
        if (locker.getAssignedUser() == null || !locker.getAssignedUser().getId().equals(userId)) {
            throw new SecurityException("This locker is not assigned to the user with ID: " + userId);
        }

        locker.setLocked(false);
        return lockerRepository.save(locker);
    }
    
    // New method for SMS-based unlocking
    public boolean unlockLockerViaSms(String phoneNumber, Long lockerId, String pin) {
        // Look up the user by their phone number (make sure UserRepository has this method)
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        
        // TODO: Validate the provided pin against the user's stored PIN or another mechanism.
        // For now, we'll assume the pin is valid.
        try {
            unlockLocker(lockerId, user.getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // New method for SMS-based locking; assumes a similar locking operation is desired.
    public boolean lockLockerViaSms(String phoneNumber, Long lockerId, String pin) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        
        // TODO: Validate the provided pin.
        try {
            lockLocker(lockerId, user.getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper method to lock a locker
    public Locker lockLocker(Long lockerId, Long userId) {
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found with ID: " + lockerId));
        if (locker.getAssignedUser() == null || !locker.getAssignedUser().getId().equals(userId)) {
            throw new SecurityException("This locker is not assigned to the user with ID: " + userId);
        }
        locker.setLocked(true);
        return lockerRepository.save(locker);
    }
}
