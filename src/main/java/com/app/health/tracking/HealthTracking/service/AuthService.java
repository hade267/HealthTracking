package com.app.health.tracking.HealthTracking.service;

import com.app.health.tracking.HealthTracking.dto.AuthRequest;
import com.app.health.tracking.HealthTracking.dto.AuthResponse;
import com.app.health.tracking.HealthTracking.dto.RegisterRequest;
import com.app.health.tracking.HealthTracking.model.Users;
import com.app.health.tracking.HealthTracking.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users register(RegisterRequest request) {
        logger.debug("Registering new user: {}", request.getUsername());
        if (usersRepository.findByUsername(request.getUsername()) != null) {
            logger.warn("Username {} already exists", request.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (usersRepository.findByEmail(request.getEmail()) != null) {
            logger.warn("Email {} already exists", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Users.Role.USER);
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        user.setUpdateAt(new Timestamp(System.currentTimeMillis())); // Set updateAt on creation
        user.setRefreshToken(UUID.randomUUID().toString());
        Users savedUser = usersRepository.save(user);
        logger.info("Successfully registered user: {} with refresh token", savedUser.getUsername());
        return savedUser;
    }

    public AuthResponse login(AuthRequest request) {
        logger.debug("Logging in user: {}", request.getUsername());
        Users user = usersRepository.findByUsername(request.getUsername());
        if (user == null) {
            logger.warn("User not found for username: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Invalid password for username: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        user.setUpdateAt(new Timestamp(System.currentTimeMillis())); // Set updateAt on login
        usersRepository.save(user);

        logger.info("Successfully logged in user: {} with new refresh token", user.getUsername());
        return new AuthResponse(
                user.getUsername(),
                refreshToken,
                "Login successful",
                user.getRole().name()
        );
    }
}