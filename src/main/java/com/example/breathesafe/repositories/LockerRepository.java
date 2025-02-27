package com.example.breathesafe.repositories;

import com.example.breathesafe.entities.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {
    // Custom queries if needed
}
