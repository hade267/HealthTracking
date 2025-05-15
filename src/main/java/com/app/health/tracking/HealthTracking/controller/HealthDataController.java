package com.app.health.tracking.HealthTracking.controller;

import com.app.health.tracking.HealthTracking.dto.AuthRequest;
import com.app.health.tracking.HealthTracking.dto.AuthResponse;
import com.app.health.tracking.HealthTracking.dto.RegisterRequest;
import com.app.health.tracking.HealthTracking.model.Devices;
import com.app.health.tracking.HealthTracking.model.HealthData;
import com.app.health.tracking.HealthTracking.model.Users;
import com.app.health.tracking.HealthTracking.repository.DevicesRepository;
import com.app.health.tracking.HealthTracking.repository.HealthDataRepository;
import com.app.health.tracking.HealthTracking.repository.UsersRepository;
import com.app.health.tracking.HealthTracking.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class HealthDataController {

    private static final Logger logger = LoggerFactory.getLogger(HealthDataController.class);

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private DevicesRepository devicesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthService authService;

    // Existing Health Data Endpoints
    @GetMapping("/health/data/{userId}")
    public List<HealthData> getHealthData(@PathVariable Long userId, HttpServletRequest request) {
        logger.debug("Fetching health data for userId: {}", userId);
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");

            if (!authenticatedUserId.equals(userId) && !"ADMIN".equals(role)) {
                logger.error("User {} with role {} attempted to access health data of user {}. Access denied.", authenticatedUserId, role, userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own health data");
            }

            List<HealthData> data = healthDataRepository.findByUserId(userId);
            logger.info("Successfully fetched {} health data entries for userId: {}", data.size(), userId);
            return data;
        } catch (Exception e) {
            logger.error("Failed to fetch health data for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch health data: " + e.getMessage());
        }
    }

    @PostMapping("/health/data")
    public HealthData saveHealthData(@RequestBody @Valid HealthData healthData, HttpServletRequest request) {
        logger.debug("Saving health data for userId: {}", healthData.getUserId());
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");

            if (!authenticatedUserId.equals(healthData.getUserId()) && !"ADMIN".equals(role)) {
                logger.error("User {} with role {} attempted to save health data for user {}. Access denied.", authenticatedUserId, role, healthData.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only save your own health data");
            }

            HealthData savedData = healthDataRepository.save(healthData);
            logger.info("Successfully saved health data for userId: {}", savedData.getUserId());
            return savedData;
        } catch (Exception e) {
            logger.error("Failed to save health data for userId: {}. Error: {}", healthData.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save health data: " + e.getMessage());
        }
    }

    @GetMapping("/health/devices")
    public List<Devices> getAllDevices() {
        logger.debug("Fetching all devices");
        try {
            List<Devices> devices = devicesRepository.findAll();
            logger.info("Successfully fetched {} devices", devices.size());
            return devices;
        } catch (Exception e) {
            logger.error("Failed to fetch devices. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch devices: " + e.getMessage());
        }
    }

    // New Endpoint to Create a Device
    @PostMapping("/devices")
    public Devices createDevice(@RequestBody @Valid Devices device, HttpServletRequest request) {
        logger.debug("Creating device for userId: {}", device.getUserId());
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");

            // Ensure USER can only create devices for themselves, ADMIN can create for anyone
            if (!authenticatedUserId.equals(device.getUserId()) && !"ADMIN".equals(role)) {
                logger.error("User {} with role {} attempted to create device for user {}. Access denied.", authenticatedUserId, role, device.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only create devices for yourself");
            }

            // Verify the user exists
            if (!usersRepository.existsById(device.getUserId())) {
                logger.warn("User with id: {} not found for device creation", device.getUserId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            // Set the createdAt timestamp
            device.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            Devices savedDevice = devicesRepository.save(device);
            logger.info("Successfully created device with id: {} for userId: {}", savedDevice.getId(), savedDevice.getUserId());
            return savedDevice;
        } catch (Exception e) {
            logger.error("Failed to create device for userId: {}. Error: {}", device.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create device: " + e.getMessage());
        }
    }

    // Auth Endpoints
    @PostMapping("/auth/register")
    public Users registerUser(@RequestBody @Valid RegisterRequest request) {
        logger.debug("Processing registration request for username: {}", request.getUsername());
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@RequestBody @Valid AuthRequest request) {
        logger.debug("Processing login request for username: {}", request.getUsername());
        return authService.login(request);
    }

    // User Management Endpoints for ADMIN
    @GetMapping("/users")
    public List<Users> getAllUsers(HttpServletRequest request) {
        logger.debug("Fetching all users");
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                logger.error("Non-ADMIN user attempted to fetch all users. Role: {}", role);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this endpoint");
            }
            List<Users> users = usersRepository.findAll();
            logger.info("Successfully fetched {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to fetch users. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users: " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public Users getUserById(@PathVariable Long id, HttpServletRequest request) {
        logger.debug("Fetching user with id: {}", id);
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                logger.error("Non-ADMIN user attempted to fetch user with id: {}. Role: {}", id, role);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this endpoint");
            }
            Optional<Users> user = usersRepository.findById(id);
            if (user.isPresent()) {
                logger.info("Successfully fetched user with id: {}", id);
                return user.get();
            } else {
                logger.warn("User with id: {} not found", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch user with id: {}. Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public Users updateUser(@PathVariable Long id, @RequestBody @Valid Users updatedUser, HttpServletRequest request) {
        logger.debug("Updating user with id: {}", id);
        try {
            if (updatedUser == null) {
                logger.error("Request body is missing for updating user with id: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required to update user");
            }
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                logger.error("Non-ADMIN user attempted to update user with id: {}. Role: {}", id, role);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this endpoint");
            }
            Optional<Users> existingUser = usersRepository.findById(id);
            if (existingUser.isPresent()) {
                Users user = existingUser.get();
                user.setUsername(updatedUser.getUsername());
                user.setEmail(updatedUser.getEmail());
                user.setFullName(updatedUser.getFullName());
                user.setRole(updatedUser.getRole());
                Users savedUser = usersRepository.save(user);
                logger.info("Successfully updated user with id: {}", id);
                return savedUser;
            } else {
                logger.warn("User with id: {} not found for update", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Failed to update user with id: {}. Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id, HttpServletRequest request) {
        logger.debug("Deleting user with id: {}", id);
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                logger.error("Non-ADMIN user attempted to delete user with id: {}. Role: {}", id, role);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this endpoint");
            }
            if (usersRepository.existsById(id)) {
                usersRepository.deleteById(id);
                logger.info("Successfully deleted user with id: {}", id);
            } else {
                logger.warn("User with id: {} not found for deletion", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Failed to delete user with id: {}. Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
}