package com.app.health.tracking.HealthTracking.model;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "devices")
@Data
public class Devices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Device UUID is required")
    @Column(name = "device_uuid")
    private String deviceUuid;

    @NotBlank(message = "Device name is required")
    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;
}
