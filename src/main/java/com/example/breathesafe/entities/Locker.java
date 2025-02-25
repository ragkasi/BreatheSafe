package com.example.lockersystem.entities;

import javax.persistence.*;

@Entity
@Table(name = "lockers")
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean locked;

    // Relationship to User (the user currently assigned)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User assignedUser;

    // Constructors
    public Locker() { }

    public Locker(boolean locked) {
        this.locked = locked;
    }

    // Getters & setters
    public Long getId() { return id; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public User getAssignedUser() { return assignedUser; }
    public void setAssignedUser(User assignedUser) { this.assignedUser = assignedUser; }
}
