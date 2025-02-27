package com.example.breathesafe.controllers;

import com.example.breathesafe.entities.Locker;
import com.example.breathesafe.entities.User;
import com.example.breathesafe.services.LockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lockers")
public class LockerController {

    @Autowired
    private LockerService lockerService;

    @PostMapping("/{lockerId}/assign")
    public Locker assignLocker(@PathVariable Long lockerId, @RequestParam Long userId) {
        return lockerService.assignLocker(lockerId, userId);
    }

    @PostMapping("/{lockerId}/unlock")
    public Locker unlockLocker(@PathVariable Long lockerId, @RequestParam Long userId) {
        return lockerService.unlockLocker(lockerId, userId);
    }
}
