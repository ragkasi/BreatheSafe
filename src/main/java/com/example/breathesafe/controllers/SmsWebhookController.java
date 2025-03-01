package com.example.breathesafe.controllers;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.breathesafe.entities.Locker;
import com.example.breathesafe.entities.User;
import com.example.breathesafe.services.LockerService;
import com.example.breathesafe.services.SmsService;
import com.example.breathesafe.services.UserService;

@RestController
@RequestMapping("/api")
public class SmsWebhookController {

    private final LockerService lockerService;
    private final SmsService smsService;
    private final UserService userService;

    public SmsWebhookController(LockerService lockerService, SmsService smsService, UserService userService) {
        this.lockerService = lockerService;
        this.smsService = smsService;
        this.userService = userService;
    }

    /**
     * Receives an SMS from Twilio.
     * Supported commands:
     *  - "START": Initiates registration.
     *  - If the user is in an incomplete registration state (record exists but name is empty),
     *    the entire message is treated as their full name.
     *  - "LOCK <PIN>": Sets the user's PIN (hashed) and locks their assigned locker.
     *  - "UNLOCK <PIN>": Unlocks the locker if the provided PIN matches, then clears the PIN.
     *  - "RELEASE <lockerNumber>": Unassigns the locker and deletes the user from the DB.
     *  - "LOCKER": Returns the assigned locker number.
     *  - Otherwise, unrecognized commands are reported as errors.
     */
    @PostMapping(value = "/sms-webhook", produces = MediaType.APPLICATION_XML_VALUE)
    public String receiveSms(@RequestParam("From") String from,
                             @RequestParam("Body") String body) {
        System.out.println("Received SMS from: " + from);
        System.out.println("Message body: " + body);

        String trimmed = body.trim();
        String[] parts = trimmed.split("\\s+");
        String command = parts[0].toUpperCase();

        // Look up a user by phone number.
        Optional<User> userOpt = userService.findByPhoneNumber(from);

        // Process "START" command.
        if (command.equals("START")) {
            if (userOpt.isPresent() && userOpt.get().getName() != null && !userOpt.get().getName().trim().isEmpty()) {
                Optional<Locker> lockerOpt = lockerService.getLockerForUser(userOpt.get().getId());
                String msg = "You are already registered" + (lockerOpt.isPresent() ? " with locker " + lockerOpt.get().getId() : "");
                smsService.sendSms(from, msg);
                return "<Response><Message>" + msg + "</Message></Response>";
            } else {
                // If no record exists, or if it exists with an empty name, create/update placeholder.
                if (!userOpt.isPresent()) {
                    User placeholder = new User();
                    placeholder.setPhoneNumber(from);
                    placeholder.setName(""); // empty name indicates incomplete registration
                    placeholder.setHashedPin("");
                    userOpt = Optional.of(userService.createUser(placeholder));
                }
                String msg = "Please reply with your full name to complete registration.";
                smsService.sendSms(from, msg);
                return "<Response><Message>" + msg + "</Message></Response>";
            }
        }
        
        // If a record exists but the name is empty, treat the entire message as the user's name.
        if (userOpt.isPresent() && (userOpt.get().getName() == null || userOpt.get().getName().trim().isEmpty())) {
            String fullName = trimmed;
            userService.updateUserName(userOpt.get(), fullName);
            try {
                Locker assignedLocker = lockerService.assignLockerToNewUser(userOpt.get());
                String msg = "Registration successful. Your locker number is " + assignedLocker.getId()
                           + ". To lock your locker, text: LOCK <PIN>";
                smsService.sendSms(from, msg);
                return "<Response><Message>" + msg + "</Message></Response>";
            } catch (Exception e) {
                String msg = "No lockers available. Please try again later.";
                smsService.sendSms(from, msg);
                userService.deleteUser(userOpt.get());
                return "<Response><Message>" + msg + "</Message></Response>";
            }
        }
        
        // If the user record still doesn't exist or is incomplete, prompt registration.
        if (!userOpt.isPresent() || userOpt.get().getName() == null || userOpt.get().getName().trim().isEmpty()) {
            String msg = "You are not registered. Please send 'START' to begin registration.";
            smsService.sendSms(from, msg);
            return "<Response><Message>" + msg + "</Message></Response>";
        }

        // User is fully registered; process other commands.
        User user = userOpt.get();
        switch (command) {
            case "LOCK":
                if (parts.length != 2) {
                    String msg = "Error: Invalid format for LOCK. Expected: LOCK <PIN>";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
                String lockPin = parts[1];
                userService.updateUserPin(user, lockPin);
                Optional<Locker> lockerForUser = lockerService.getLockerForUser(user.getId());
                if (lockerForUser.isPresent()) {
                    lockerService.lockLocker(lockerForUser.get().getId(), user.getId());
                    String msg = "Locker locked successfully. Your locker number is " + lockerForUser.get().getId()
                               + ". To unlock, text: UNLOCK <PIN> (your current PIN).";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                } else {
                    String msg = "Error: No locker assigned to you.";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
            case "UNLOCK":
                if (parts.length != 2) {
                    String msg = "Error: Invalid format for UNLOCK. Expected: UNLOCK <PIN>";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
                String unlockPin = parts[1];
                if (userService.verifyUserPin(user, unlockPin)) {
                    Optional<Locker> lockerOpt2 = lockerService.getLockerForUser(user.getId());
                    if (lockerOpt2.isPresent()) {
                        lockerService.unlockLocker(lockerOpt2.get().getId(), user.getId());
                        // Clear the user's PIN after unlocking.
                        userService.clearUserPin(user);
                        String msg = "Locker unlocked successfully. Your locker number is " + lockerOpt2.get().getId()
                                   + ". To release your locker, text: RELEASE " + lockerOpt2.get().getId();
                        smsService.sendSms(from, msg);
                        return "<Response><Message>" + msg + "</Message></Response>";
                    } else {
                        String msg = "Error: No locker assigned to you.";
                        smsService.sendSms(from, msg);
                        return "<Response><Message>" + msg + "</Message></Response>";
                    }
                } else {
                    String msg = "Error: Invalid PIN for unlocking.";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
            case "RELEASE":
                if (parts.length != 2) {
                    String msg = "Error: Invalid format for RELEASE. Expected: RELEASE <lockerNumber>";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
                String lockerNumberStr = parts[1];
                Optional<Locker> lockerOpt3 = lockerService.getLockerForUser(user.getId());
                if (lockerOpt3.isPresent() && lockerOpt3.get().getId().toString().equals(lockerNumberStr)) {
                    lockerService.unassignLockerFromUser(user);
                    userService.deleteUser(user);
                    String msg = "Your locker (" + lockerOpt3.get().getId() + ") has been unlinked and your registration released.";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                } else {
                    String msg = "Error: Provided locker number does not match your assigned locker.";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
            case "LOCKER":
                Optional<Locker> lockerOpt4 = lockerService.getLockerForUser(user.getId());
                if (lockerOpt4.isPresent()) {
                    String msg = "Your locker number is " + lockerOpt4.get().getId();
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                } else {
                    String msg = "You do not have a locker assigned.";
                    smsService.sendSms(from, msg);
                    return "<Response><Message>" + msg + "</Message></Response>";
                }
            default:
                String msg = "Error: Unrecognized command.";
                smsService.sendSms(from, msg);
                return "<Response><Message>" + msg + "</Message></Response>";
        }
    }
}
