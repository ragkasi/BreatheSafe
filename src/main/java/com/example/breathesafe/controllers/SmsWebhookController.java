package com.example.breathesafe.controllers;


// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;

import com.example.breathesafe.services.LockerService;

@RestController
@RequestMapping("/api")
public class SmsWebhookController {

    private final LockerService lockerService;

    // Constructor-based dependency injection
    public SmsWebhookController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    /**
     * Receives an SMS from Twilio.
     * Expected message format: "UNLOCK 1234 PIN123" or "LOCK 1234 PIN123"
     */
    @PostMapping(value = "/sms-webhook", produces = MediaType.APPLICATION_XML_VALUE)
    public String receiveSms(@RequestParam("From") String from,
                             @RequestParam("Body") String body) {
        // Trim and split the body by whitespace
        String[] parts = body.trim().split("\\s+");
        if (parts.length < 3) {
            return "<Response><Message>Invalid command format. Expected: UNLOCK <lockerId> <PIN></Message></Response>";
        }

        // Print incoming text messages to the console
        System.out.print("Received SMS from: " + from);
        System.out.print("Message body: " + body);

        String command = parts[0].toUpperCase();
        String lockerIdStr = parts[1];
        String pin = parts[2];

        try {
            Long lockerId = Long.parseLong(lockerIdStr);
            boolean success = false;
            if ("UNLOCK".equals(command)) {
                // Call your service to attempt unlocking
                success = lockerService.unlockLockerViaSms(from, lockerId, pin);
            } else if ("LOCK".equals(command)) {
                // Call your service to attempt locking
                success = lockerService.lockLockerViaSms(from, lockerId, pin);
            } else {
                return "<Response><Message>Unknown command. Use LOCK or UNLOCK.</Message></Response>";
            }

            if (success) {
                return "<Response><Message>Command processed successfully.</Message></Response>";
            } else {
                return "<Response><Message>Failed to process command. Please check your PIN or assignment.</Message></Response>";
            }
        } catch (NumberFormatException e) {
            return "<Response><Message>Invalid locker ID format.</Message></Response>";
        }
    }
}
