package com.example.lockersystem.services;

import com.example.lockersystem.entities.Locker;
import com.example.lockersystem.entities.User;
import com.example.lockersystem.repositories.LockerRepository;
import com.example.lockersystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        
        if (locker.getAssignedUser() == null
                || !locker.getAssignedUser().getId().equals(userId)) {
            throw new SecurityException("This locker is not assigned to the user with ID: " + userId);
        }

        locker.setLocked(false);
        return lockerRepository.save(locker);
    }
}
