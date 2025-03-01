package com.example.breathesafe.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.breathesafe.entities.Locker;

@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {
    Optional<Locker> findByAssignedUserId(Long userId);
    
    // New method: find the first locker with no assigned user
    Optional<Locker> findFirstByAssignedUserIsNull();
}
