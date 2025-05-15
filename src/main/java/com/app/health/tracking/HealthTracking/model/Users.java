package com.app.health.tracking.HealthTracking.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "refresh_token")
    private String refreshToken;

    public enum Role {
        USER, ADMIN
    }
}
