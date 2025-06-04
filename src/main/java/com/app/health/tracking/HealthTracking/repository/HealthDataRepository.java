package com.app.health.tracking.HealthTracking.repository;

import com.app.health.tracking.HealthTracking.model.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {
    List<HealthData> findByUserId(Long userId);
    Optional<HealthData> findByUserIdAndDataDate(Long userId, LocalDate dataDate);
}
