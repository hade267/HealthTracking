package com.app.health.tracking.HealthTracking.service;

import com.app.health.tracking.HealthTracking.dto.AuthRequest;
import com.app.health.tracking.HealthTracking.dto.AuthResponse;
import com.app.health.tracking.HealthTracking.dto.RegisterRequest;
import com.app.health.tracking.HealthTracking.jwt.JwtUtil;
import com.app.health.tracking.HealthTracking.model.Users;
import com.app.health.tracking.HealthTracking.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Users register(RegisterRequest request) {
        logger.debug("Registering new user: {}", request.getUsername());
        if (usersRepository.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Users.Role.USER);
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Users savedUser = usersRepository.save(user);
        logger.info("Successfully registered user: {}", savedUser.getUsername());
        return savedUser;
    }

    public AuthResponse login(AuthRequest request) {
        logger.debug("Logging in user: {}", request.getUsername());
        Users user = usersRepository.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Invalid login attempt for username: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        logger.info("Successfully logged in user: {} with token: {}", user.getUsername(),token);
        return new AuthResponse(user.getUsername(), token);
    }
}