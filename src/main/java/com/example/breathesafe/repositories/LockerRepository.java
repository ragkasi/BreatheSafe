package com.example.lockersystem.repositories;

import com.example.lockersystem.entities.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {
    // Custom queries if needed
}
