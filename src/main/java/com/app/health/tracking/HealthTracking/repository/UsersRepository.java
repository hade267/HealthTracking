package com.app.health.tracking.HealthTracking.repository;

import com.app.health.tracking.HealthTracking.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
    Users findByRefreshToken(String refreshToken);
    Users findByEmail(String email);
}
