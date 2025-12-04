package com.example.rest_service.service;

import com.example.rest_service.SupabaseConfig;
import com.example.rest_service.model.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class UserProfileService {

    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserProfileService(RestTemplate restTemplate, SupabaseConfig supabaseConfig) {
        this.restTemplate = restTemplate;
        this.supabaseConfig = supabaseConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Get all user profiles
     */
    public List<UserProfile> getAllUsers() {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles";
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                UserProfile[].class
            );
            
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user profiles: " + e.getMessage(), e);
        }
    }

    /**
     * Get user profile by ID
     */
    public Optional<UserProfile> getUserById(UUID id) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?id=eq." + id;
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                UserProfile[].class
            );
            
            UserProfile[] users = response.getBody();
            if (users != null && users.length > 0) {
                return Optional.of(users[0]);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user profile by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Get user profile by email
     */
    public Optional<UserProfile> getUserByEmail(String email) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?email=eq." + email;
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                UserProfile[].class
            );
            
            UserProfile[] users = response.getBody();
            if (users != null && users.length > 0) {
                return Optional.of(users[0]);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user profile by email: " + e.getMessage(), e);
        }
    }

    /**
     * Get user profile by Google ID
     */
    public Optional<UserProfile> getUserByGoogleId(String googleId) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?google_id=eq." + googleId;
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                UserProfile[].class
            );
            
            UserProfile[] users = response.getBody();
            if (users != null && users.length > 0) {
                return Optional.of(users[0]);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user profile by Google ID: " + e.getMessage(), e);
        }
    }

    /**
     * Get user profile by GitHub ID
     */
    public Optional<UserProfile> getUserByGithubId(String githubId) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?github_id=eq." + githubId;
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                UserProfile[].class
            );
            
            UserProfile[] users = response.getBody();
            if (users != null && users.length > 0) {
                return Optional.of(users[0]);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user profile by GitHub ID: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new user profile
     */
    public UserProfile createUser(UserProfile userProfile) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            
            // Convert UserProfile to Map for JSON serialization
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", userProfile.getEmail());
            userData.put("google_id", userProfile.getGoogleId());
            userData.put("github_id", userProfile.getGithubId());
            userData.put("name", userProfile.getName());
            userData.put("first_name", userProfile.getFirstName());
            userData.put("last_name", userProfile.getLastName());
            userData.put("profile_picture_url", userProfile.getProfilePictureUrl());
            userData.put("username", userProfile.getUsername());
            userData.put("bio", userProfile.getBio());
            userData.put("obrobucks", userProfile.getObrobucks() != null ? userProfile.getObrobucks() : 0);
            userData.put("access_token", userProfile.getAccessToken());
            userData.put("refresh_token", userProfile.getRefreshToken());
            userData.put("token_expires_at", userProfile.getTokenExpiresAt());
            // Only set last_login if it's provided, otherwise let database handle it
            if (userProfile.getLastLogin() != null) {
                userData.put("last_login", userProfile.getLastLogin());
            }
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userData, headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles";
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                UserProfile[].class
            );
            
            UserProfile[] createdUsers = response.getBody();
            if (createdUsers != null && createdUsers.length > 0) {
                return createdUsers[0];
            }
            throw new RuntimeException("Failed to create user profile");
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error creating user profile: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error creating user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Update user profile
     */
    public UserProfile updateUser(UUID id, UserProfile userProfile) {
        try {
            System.out.println("UserProfileService.updateUser called for ID: " + id);
            
            // Build update payload with ONLY updatable fields
            // Explicitly exclude read-only fields: id, email, google_id, created_at, updated_at, last_login
            Map<String, Object> userData = new HashMap<>();
            if (userProfile.getName() != null) userData.put("name", userProfile.getName());
            if (userProfile.getFirstName() != null) userData.put("first_name", userProfile.getFirstName());
            if (userProfile.getLastName() != null) userData.put("last_name", userProfile.getLastName());
            if (userProfile.getProfilePictureUrl() != null) userData.put("profile_picture_url", userProfile.getProfilePictureUrl());
            if (userProfile.getUsername() != null) userData.put("username", userProfile.getUsername());
            if (userProfile.getBio() != null) userData.put("bio", userProfile.getBio());
            if (userProfile.getObrobucks() != null) userData.put("obrobucks", userProfile.getObrobucks());
            if (userProfile.getAccessToken() != null) userData.put("access_token", userProfile.getAccessToken());
            if (userProfile.getRefreshToken() != null) userData.put("refresh_token", userProfile.getRefreshToken());
            if (userProfile.getTokenExpiresAt() != null) userData.put("token_expires_at", userProfile.getTokenExpiresAt());
            
            System.out.println("Update payload (updatable fields only): " + userData);
            
            // Use special headers for PATCH operations
            HttpHeaders patchHeaders = supabaseConfig.createSupabaseHeadersForUpdate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userData, patchHeaders);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?id=eq." + id;
            System.out.println("Sending PATCH request to: " + url);
            
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.PATCH, 
                entity, 
                UserProfile[].class
            );
            
            System.out.println("Supabase response status: " + response.getStatusCode());
            UserProfile[] updatedUsers = response.getBody();
            if (updatedUsers != null && updatedUsers.length > 0) {
                System.out.println("User updated successfully");
                return updatedUsers[0];
            }
            throw new RuntimeException("Failed to update user profile - no user returned from database");
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error updating user: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error updating user profile (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            System.err.println("Exception updating user: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Update user's last login time
     */
    public void updateLastLogin(UUID id) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeadersForUpdate();
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("last_login", OffsetDateTime.now());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userData, headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?id=eq." + id;
            restTemplate.exchange(
                url, 
                HttpMethod.PATCH, 
                entity, 
                String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Error updating last login: " + e.getMessage(), e);
        }
    }

    /**
     * Delete user profile
     */
    public boolean deleteUser(UUID id) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?id=eq." + id;
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.DELETE, 
                entity, 
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Search users by username
     */
    public List<UserProfile> searchUsersByUsername(String username) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/user_profiles?username=ilike.*" + username + "*";
            ResponseEntity<UserProfile[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                UserProfile[].class
            );
            
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error searching users by username: " + e.getMessage(), e);
        }
    }
}