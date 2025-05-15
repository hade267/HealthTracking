package com.app.health.tracking.HealthTracking.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "health_data")
@Data
public class HealthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "sleep_hours")
    private Float sleepHours;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "systolic")
    private Integer systolic;

    @Column(name = "diastolic")
    private Integer diastolic;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "mood")
    @Enumerated(EnumType.STRING)
    private Mood mood;

    @Column(name = "data_date")
    private java.sql.Date dataDate;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    public enum Mood {
        GOOD, NORMAL, BAD
    }
}
