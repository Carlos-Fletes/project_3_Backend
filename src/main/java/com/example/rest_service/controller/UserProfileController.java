package com.example.rest_service.controller;

import com.example.rest_service.model.UserProfile;
import com.example.rest_service.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Configure this properly for production
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        try {
            List<UserProfile> users = userProfileService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserById(@PathVariable String id) {
        try {
            UUID userId = UUID.fromString(id);
            Optional<UserProfile> user = userProfileService.getUserById(userId);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user by email
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserProfile> getUserByEmail(@PathVariable String email) {
        try {
            Optional<UserProfile> user = userProfileService.getUserByEmail(email);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user by Google ID
     * GET /api/users/google/{googleId}
     */
    @GetMapping("/google/{googleId}")
    public ResponseEntity<UserProfile> getUserByGoogleId(@PathVariable String googleId) {
        try {
            Optional<UserProfile> user = userProfileService.getUserByGoogleId(googleId);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search users by username
     * GET /api/users/search?username=value
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserProfile>> searchUsers(@RequestParam String username) {
        try {
            List<UserProfile> users = userProfileService.searchUsersByUsername(username);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserProfile userProfile) {
        try {
            System.out.println("Creating user with payload: " + userProfile.toString());
            UserProfile createdUser = userProfileService.createUser(userProfile);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Update user profile
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateUser(@PathVariable String id, @RequestBody UserProfile userProfile) {
        try {
            UUID userId = UUID.fromString(id);
            UserProfile updatedUser = userProfileService.updateUser(userId, userProfile);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user's last login
     * PATCH /api/users/{id}/login
     */
    @PatchMapping("/{id}/login")
    public ResponseEntity<Void> updateLastLogin(@PathVariable String id) {
        try {
            UUID userId = UUID.fromString(id);
            userProfileService.updateLastLogin(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            UUID userId = UUID.fromString(id);
            boolean deleted = userProfileService.deleteUser(userId);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/users/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("User Profile Service is running");
    }
}