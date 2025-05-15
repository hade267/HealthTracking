package com.app.health.tracking.HealthTracking.model;
import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users")
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(name = "username")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @NotNull(message = "Role is required")
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name")
    private String fullName;

    public enum Role {
        USER, ADMIN
    }
}
