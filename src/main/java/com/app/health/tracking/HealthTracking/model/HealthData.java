package com.app.health.tracking.HealthTracking.model;

import lombok.Data;
import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "health_data")
@Data
public class HealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "sleep_hours")
    private Double sleepHours;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "systolic")
    private Integer systolic;

    @Column(name = "diastolic")
    private Integer diastolic;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "temperature")
    private Double temperature;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood")
    private Mood mood;

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "update_at")
    private Timestamp updateAt;

    public enum Mood {
        GOOD, NORMAL, BAD
    }
}
