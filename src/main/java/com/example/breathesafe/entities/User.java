package com.example.lockersystem.entities;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private String name;
    private String hashedPin; // Always store hashed

    // Constructors
    public User() { }
    
    public User(String phoneNumber, String name, String hashedPin) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.hashedPin = hashedPin;
    }

    // Getters & setters
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHashedPin() { return hashedPin; }
    public void setHashedPin(String hashedPin) { this.hashedPin = hashedPin; }
}
