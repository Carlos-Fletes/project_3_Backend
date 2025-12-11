package com.example.rest_service.controller;

import com.example.rest_service.model.UserProfile;
import com.example.rest_service.service.UserProfileService;
import com.example.rest_service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Configure this properly for production
public class UserProfileController {

    private final UserProfileService userProfileService;

    // Inject GitHub credentials from application.properties
    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

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
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserProfile userProfile) {
        try {
            System.out.println("Updating user " + id + " with payload: " + userProfile.toString());
            UUID userId = UUID.fromString(id);
            UserProfile updatedUser = userProfileService.updateUser(userId, userProfile);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid user ID format: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user: " + e.getMessage());
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

    /**
     * ✅ GitHub OAuth login
     * POST /api/users/auth/github
     *
     * Body: { "code": "<github_auth_code_from_expo>" }
     */
    @PostMapping("/auth/github")
    public ResponseEntity<?> githubLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'code' in request body");
        }

        try {
            // 1. Exchange code for access token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> params = new HashMap<>();
            params.put("client_id", githubClientId);
            params.put("client_secret", githubClientSecret);
            params.put("code", code);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse =
                    restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Failed to exchange code for access token");
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("GitHub did not return an access token");
            }

            // 2. Fetch GitHub user profile
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            userHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    userRequest,
                    Map.class
            );

            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Failed to fetch GitHub user profile");
            }

            Map<String, Object> gh = userResponse.getBody();

            Object rawName = gh.get("name");
            String name = rawName != null ? rawName.toString() : null;
            String githubId = gh.get("id") != null ? gh.get("id").toString() : null;
            String username = (String) gh.get("login");
            String avatarUrl = (String) gh.get("avatar_url");
            String bio = (String) gh.get("bio");
            String email = (String) gh.get("email"); // may be null
            
            // Generate default email if not provided by GitHub
            if (email == null || email.isBlank()) {
                email = username + "@github.local";
            }

            // 3. Persist via Supabase through service layer
            UserProfile userProfile = userProfileService.createOrUpdateGithubUser(
                    githubId,
                    email,
                    name != null ? name : username, // fallback if name is null
                    username,
                    avatarUrl,
                    bio,
                    accessToken
            );

            // 4. Generate JWT token for authentication
            String jwtToken = JwtUtil.generateToken(userProfile.getEmail());
            
            // 5. Shape response for frontend
            Map<String, Object> result = new HashMap<>();
            result.put("id", userProfile.getId());
            result.put("name", userProfile.getName());
            result.put("username", userProfile.getUsername());
            result.put("profile_picture_url", userProfile.getProfilePictureUrl());
            result.put("bio", userProfile.getBio());
            result.put("obrobucks", userProfile.getObrobucks());
            result.put("github_id", userProfile.getGithubId());
            result.put("email", userProfile.getEmail());
            result.put("token", jwtToken);  // ← JWT token for frontend authentication

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("GitHub login failed: " + e.getMessage());
        }
    }
}
