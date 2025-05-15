package com.app.health.tracking.HealthTracking.service;

import com.app.health.tracking.HealthTracking.dto.AuthRequest;
import com.app.health.tracking.HealthTracking.dto.AuthResponse;
import com.app.health.tracking.HealthTracking.dto.RegisterRequest;
import com.app.health.tracking.HealthTracking.model.Users;
import com.app.health.tracking.HealthTracking.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users register(RegisterRequest request) {
        logger.debug("Registering user with username: {}", request.getUsername());
        try {
            Users user = new Users();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setRole(Users.Role.USER);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            Users savedUser = usersRepository.save(user);
            logger.info("Successfully registered user with username: {}", savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to register user with username: {}. Error: {}", request.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse login(AuthRequest request) {
        logger.debug("Attempting login for username: {}", request.getUsername());
        try {
            Users user = usersRepository.findByUsername(request.getUsername());
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.error("Login failed for username: {}. Invalid credentials", request.getUsername());
                throw new RuntimeException("Invalid username or password");
            }
            String token = UUID.randomUUID().toString();
            user.setRefreshToken(token);
            usersRepository.save(user);
            AuthResponse response = new AuthResponse();
            response.setUsername(user.getUsername());
            response.setToken(token);
            logger.info("Successfully logged in user: {}", user.getUsername());
            return response;
        } catch (Exception e) {
            logger.error("Login failed for username: {}. Error: {}", request.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
}