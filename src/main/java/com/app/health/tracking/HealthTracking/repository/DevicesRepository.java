package com.app.health.tracking.HealthTracking.repository;

import com.app.health.tracking.HealthTracking.model.Devices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevicesRepository extends JpaRepository<Devices, Long> {
    List<Devices> findByUserId(Long userId);
}
