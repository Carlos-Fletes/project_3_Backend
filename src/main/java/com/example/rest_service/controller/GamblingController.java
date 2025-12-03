package com.example.rest_service.controller;

import com.example.rest_service.service.UserProfileService;
import com.example.rest_service.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/gambling")
public class GamblingController {

    private final UserProfileService userProfileService;

    @Autowired
    public GamblingController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Open a loot box and update user's balance
     * POST /api/gambling/open-lootbox
     */
    @PostMapping("/open-lootbox")
    public ResponseEntity<Map<String, Object>> openLootbox(@RequestBody Map<String, Object> request) {
        try {
            // Extract request data
            String userIdStr = (String) request.get("userId");
            UUID userId = UUID.fromString(userIdStr);
            int cost = ((Number) request.get("cost")).intValue();
            int winAmount = ((Number) request.get("winAmount")).intValue();
            
            System.out.println("Opening loot box for user: " + userId + ", cost: " + cost + ", winAmount: " + winAmount);
            
            // Get current user
            Optional<UserProfile> userOpt = userProfileService.getUserById(userId);
            if (!userOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            UserProfile user = userOpt.get();
            int currentBalance = user.getObrobucks() != null ? user.getObrobucks() : 0;
            
            System.out.println("Current balance: " + currentBalance);
            
            // Check if user has enough funds
            if (currentBalance < cost) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Insufficient funds");
                errorResponse.put("currentBalance", currentBalance);
                errorResponse.put("required", cost);
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            // Calculate new balance
            int newBalance = currentBalance - cost + winAmount;
            int profit = winAmount - cost;
            
            System.out.println("New balance will be: " + newBalance + ", profit: " + profit);
            
            // Update user balance
            UserProfile updatedUser = new UserProfile();
            updatedUser.setObrobucks(newBalance);
            userProfileService.updateUser(userId, updatedUser);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newBalance", newBalance);
            response.put("profit", profit);
            response.put("winAmount", winAmount);
            response.put("cost", cost);
            
            System.out.println("Loot box opened successfully. Response: " + response);
            
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid request format");
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Error opening loot box: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to open loot box: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get current user balance
     * GET /api/gambling/balance/{userId}
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            Optional<UserProfile> userOpt = userProfileService.getUserById(userUUID);
            
            if (!userOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            UserProfile user = userOpt.get();
            int balance = user.getObrobucks() != null ? user.getObrobucks() : 0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("balance", balance);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching balance: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch balance");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}