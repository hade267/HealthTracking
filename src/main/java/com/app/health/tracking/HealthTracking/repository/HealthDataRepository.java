package com.app.health.tracking.HealthTracking.repository;

import com.app.health.tracking.HealthTracking.model.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {
    List<HealthData> findByUserId(Long userId);
}
