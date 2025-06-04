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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch health data: " + e.getMessage());
        }
    }

    @PostMapping("/health/data")
    public AuthResponse saveHealthData(@RequestBody @Valid HealthData healthData, HttpServletRequest request) {
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
            return new AuthResponse(
                    null,
                    null,
                    "Health data saved successfully",
                    null
            );
        } catch (Exception e) {
            logger.error("Failed to save health data for userId: {}. Error: {}", healthData.getUserId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save health data: " + e.getMessage());
        }
    }

    @PutMapping("/health/data/me")
    public AuthResponse updateHealthDataForCurrentUser(@RequestBody @Valid HealthData updatedHealthData, HttpServletRequest request) {
        logger.debug("Updating health data for logged-in user");
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            if (authenticatedUserId == null) {
                logger.error("No authenticated user found");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
            }

            // Ensure the userId in the request body matches the authenticated user
            if (!authenticatedUserId.equals(updatedHealthData.getUserId())) {
                logger.error("User {} attempted to update health data for user {}. Access denied.", authenticatedUserId, updatedHealthData.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own health data");
            }

            // Find the most recent health data for the user on the given dataDate
            Optional<HealthData> existingHealthData = healthDataRepository
                    .findByUserIdAndDataDate(authenticatedUserId, updatedHealthData.getDataDate());

            HealthData healthData;
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            if (existingHealthData.isPresent()) {
                // Update existing health data, preserve createdAt, set updateAt
                healthData = existingHealthData.get();
                healthData.setDeviceId(updatedHealthData.getDeviceId());
                healthData.setHeartRate(updatedHealthData.getHeartRate());
                healthData.setSleepHours(updatedHealthData.getSleepHours());
                healthData.setCalories(updatedHealthData.getCalories());
                healthData.setSteps(updatedHealthData.getSteps());
                healthData.setSystolic(updatedHealthData.getSystolic());
                healthData.setDiastolic(updatedHealthData.getDiastolic());
                healthData.setWeight(updatedHealthData.getWeight());
                healthData.setTemperature(updatedHealthData.getTemperature());
                healthData.setMood(updatedHealthData.getMood());
                healthData.setDataDate(updatedHealthData.getDataDate());
                healthData.setUpdateAt(currentTimestamp); // Set updateAt on update
                logger.info("Successfully updated health data for userId: {} on dataDate: {}", authenticatedUserId, updatedHealthData.getDataDate());
            } else {
                // Create new health data if none exists for the dataDate
                healthData = updatedHealthData;
                healthData.setCreatedAt(currentTimestamp); // Set createdAt for new entry
                healthData.setUpdateAt(currentTimestamp); // Set updateAt for new entry
                logger.info("No health data found for userId: {} on dataDate: {}. Creating new entry.", authenticatedUserId, updatedHealthData.getDataDate());
            }

            healthDataRepository.save(healthData);
            return new AuthResponse(
                    null,
                    null,
                    existingHealthData.isPresent() ? "Health data updated successfully" : "Health data created successfully",
                    null
            );
        } catch (Exception e) {
            logger.error("Failed to update health data for user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update health data: " + e.getMessage());
        }
    }

    @DeleteMapping("/health/data/{id}")
    public AuthResponse deleteHealthData(@PathVariable Long id, HttpServletRequest request) {
        logger.debug("Deleting health data with id: {}", id);
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");

            Optional<HealthData> healthData = healthDataRepository.findById(id);
            if (healthData.isEmpty()) {
                logger.warn("Health data with id: {} not found", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Health data not found");
            }

            if (!authenticatedUserId.equals(healthData.get().getUserId()) && !"ADMIN".equals(role)) {
                logger.error("User {} with role {} attempted to delete health data of user {}. Access denied.", authenticatedUserId, role, healthData.get().getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own health data");
            }

            healthDataRepository.deleteById(id);
            logger.info("Successfully deleted health data with id: {}", id);
            return new AuthResponse(
                    null,
                    null,
                    "Health data deleted successfully",
                    null
            );
        } catch (Exception e) {
            logger.error("Failed to delete health data with id: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete health data: " + e.getMessage());
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch devices: " + e.getMessage());
        }
    }

    @PostMapping("/devices")
    public Devices createDevice(@RequestBody @Valid Devices device, HttpServletRequest request) {
        logger.debug("Creating device for userId: {}", device.getUserId());
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");

            if (!authenticatedUserId.equals(device.getUserId()) && !"ADMIN".equals(role)) {
                logger.error("User {} with role {} attempted to create device for user {}. Access denied.", authenticatedUserId, role, device.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only create devices for yourself");
            }

            if (!usersRepository.existsById(device.getUserId())) {
                logger.warn("User with id: {} not found for device creation", device.getUserId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            device.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            Devices savedDevice = devicesRepository.save(device);
            logger.info("Successfully created device with id: {} for userId: {}", savedDevice.getId(), savedDevice.getUserId());
            return savedDevice;
        } catch (Exception e) {
            logger.error("Failed to create device for userId: {}. Error: {}", device.getUserId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create device: " + e.getMessage());
        }
    }

    @GetMapping("/devices/user")
    public List<Devices> getDevicesByUser(HttpServletRequest request) {
        logger.debug("Fetching devices for logged-in user");
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            if (authenticatedUserId == null) {
                logger.error("No authenticated user found");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
            }

            List<Devices> devices = devicesRepository.findByUserId(authenticatedUserId);
            logger.info("Successfully fetched {} devices for userId: {}", devices.size(), authenticatedUserId);
            return devices;
        } catch (Exception e) {
            logger.error("Failed to fetch devices for user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch devices: " + e.getMessage());
        }
    }

    @GetMapping("/health/data/user")
    public List<HealthData> getHealthDataByUser(HttpServletRequest request) {
        logger.debug("Fetching health data for logged-in user");
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            if (authenticatedUserId == null) {
                logger.error("No authenticated user found");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
            }

            List<HealthData> data = healthDataRepository.findByUserId(authenticatedUserId);
            logger.info("Successfully fetched {} health data entries for userId: {}", data.size(), authenticatedUserId);
            return data;
        } catch (Exception e) {
            logger.error("Failed to fetch health data for user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch health data: " + e.getMessage());
        }
    }

    @GetMapping("/users/me")
    public Users getCurrentUser(HttpServletRequest request) {
        logger.debug("Fetching details for logged-in user");
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            if (authenticatedUserId == null) {
                logger.error("No authenticated user found");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
            }

            Optional<Users> user = usersRepository.findById(authenticatedUserId);
            if (user.isPresent()) {
                logger.info("Successfully fetched details for userId: {}", authenticatedUserId);
                return user.get();
            } else {
                logger.error("User with id: {} not found", authenticatedUserId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch user details. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch user details: " + e.getMessage());
        }
    }

    @GetMapping("/health/data/me")
    public List<HealthData> getHealthDataForCurrentUser(HttpServletRequest request) {
        logger.debug("Fetching health data for logged-in user");
        try {
            Long authenticatedUserId = (Long) request.getAttribute("userId");
            if (authenticatedUserId == null) {
                logger.error("No authenticated user found");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
            }

            List<HealthData> data = healthDataRepository.findByUserId(authenticatedUserId);
            logger.info("Successfully fetched {} health data entries for userId: {}", data.size(), authenticatedUserId);
            return data;
        } catch (Exception e) {
            logger.error("Failed to fetch health data for user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch health data: " + e.getMessage());
        }
    }

    @PostMapping("/auth/register")
    public AuthResponse registerUser(@RequestBody @Valid RegisterRequest request) {
        logger.debug("Processing registration request for username: {}", request.getUsername());
        Users user = authService.register(request);
        return new AuthResponse(
                user.getUsername(),
                user.getRefreshToken(),
                "Registration successful",
                null
        );
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@RequestBody @Valid AuthRequest request) {
        logger.debug("Processing login request for username: {}", request.getUsername());
        return authService.login(request);
    }

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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch users: " + e.getMessage());
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public AuthResponse updateUser(@PathVariable Long id, @RequestBody @Valid Users updatedUser, HttpServletRequest request) {
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
                user.setRefreshToken(updatedUser.getRefreshToken());
                user.setUpdateAt(new Timestamp(System.currentTimeMillis())); // Set updateAt on update
                Users savedUser = usersRepository.save(user);
                logger.info("Successfully updated user with id: {}", id);
                return new AuthResponse(
                        null,
                        null,
                        "User updated successfully",
                        null
                );
            } else {
                logger.warn("User with id: {} not found for update", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Failed to update user with id: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public AuthResponse deleteUser(@PathVariable Long id, HttpServletRequest request) {
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
                return new AuthResponse(
                        null,
                        null,
                        "User deleted successfully",
                        null
                );
            } else {
                logger.warn("User with id: {} not found for deletion", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Failed to delete user with id: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user: " + e.getMessage());
        }
    }
}