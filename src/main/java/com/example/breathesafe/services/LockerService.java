package com.example.breathesafe.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.breathesafe.entities.Locker;
import com.example.breathesafe.entities.User;
import com.example.breathesafe.repositories.LockerRepository;
import com.example.breathesafe.repositories.UserRepository;

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
    
    // New method for auto-assigning a locker to a new user.
    // If no locker is free, it throws an exception.
    public Locker assignLockerToNewUser(User user) {
        Optional<Locker> freeLockerOpt = lockerRepository.findFirstByAssignedUserIsNull();
        if (freeLockerOpt.isEmpty()) {
            throw new RuntimeException("No lockers available");
        }
        Locker locker = freeLockerOpt.get();
        locker.setAssignedUser(user);
        locker.setLocked(false);
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
    
    public Locker lockLocker(Long lockerId, Long userId) {
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found with ID: " + lockerId));
        if (locker.getAssignedUser() == null || !locker.getAssignedUser().getId().equals(userId)) {
            throw new SecurityException("This locker is not assigned to the user with ID: " + userId);
        }
        locker.setLocked(true);
        return lockerRepository.save(locker);
    }
    
    // Returns the locker assigned to a user.
    public Optional<Locker> getLockerForUser(Long userId) {
        return lockerRepository.findByAssignedUserId(userId);
    }
    
    // Unassigns the locker from a user.
    public void unassignLockerFromUser(User user) {
        Optional<Locker> lockerOpt = lockerRepository.findByAssignedUserId(user.getId());
        if (lockerOpt.isPresent()) {
            Locker locker = lockerOpt.get();
            locker.setAssignedUser(null);
            locker.setLocked(false);
            lockerRepository.save(locker);
        }
    }
}
