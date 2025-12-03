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
import java.util.Random;

@RestController
@RequestMapping("/api/gambling")
public class GamblingController {

    private final UserProfileService userProfileService;
    private final Random random = new Random();

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
     * Play coin flip game
     * POST /api/gambling/coin-flip
     */
    @PostMapping("/coin-flip")
    public ResponseEntity<Map<String, Object>> coinFlip(@RequestBody Map<String, Object> request) {
        try {
            String userIdStr = (String) request.get("userId");
            UUID userId = UUID.fromString(userIdStr);
            int betAmount = ((Number) request.get("betAmount")).intValue();
            String userChoice = (String) request.get("choice"); // "heads" or "tails"
            
            System.out.println("Coin flip for user: " + userId + ", bet: " + betAmount + ", choice: " + userChoice);
            
            // Get current user
            Optional<UserProfile> userOpt = userProfileService.getUserById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            UserProfile user = userOpt.get();
            int currentBalance = user.getObrobucks() != null ? user.getObrobucks() : 0;
            
            // Check if user has enough funds
            if (currentBalance < betAmount) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Insufficient funds",
                    "currentBalance", currentBalance,
                    "required", betAmount
                ));
            }
            
            // Flip the coin (50/50)
            String result = random.nextBoolean() ? "heads" : "tails";
            boolean won = result.equals(userChoice.toLowerCase());
            
            // Calculate winnings
            int winAmount = won ? betAmount * 2 : 0;
            int newBalance = currentBalance - betAmount + winAmount;
            int profit = winAmount - betAmount;
            
            // Update user balance
            UserProfile updatedUser = new UserProfile();
            updatedUser.setObrobucks(newBalance);
            userProfileService.updateUser(userId, updatedUser);
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("won", won);
            response.put("betAmount", betAmount);
            response.put("winAmount", winAmount);
            response.put("profit", profit);
            response.put("newBalance", newBalance);
            
            System.out.println("Coin flip result: " + response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in coin flip: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to play coin flip: " + e.getMessage()));
        }
    }

    /**
     * Play dice roll game
     * POST /api/gambling/dice-roll
     */
    @PostMapping("/dice-roll")
    public ResponseEntity<Map<String, Object>> diceRoll(@RequestBody Map<String, Object> request) {
        try {
            String userIdStr = (String) request.get("userId");
            UUID userId = UUID.fromString(userIdStr);
            int betAmount = ((Number) request.get("betAmount")).intValue();
            
            System.out.println("Dice roll for user: " + userId + ", bet: " + betAmount);
            
            // Get current user
            Optional<UserProfile> userOpt = userProfileService.getUserById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            UserProfile user = userOpt.get();
            int currentBalance = user.getObrobucks() != null ? user.getObrobucks() : 0;
            
            // Check if user has enough funds
            if (currentBalance < betAmount) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Insufficient funds",
                    "currentBalance", currentBalance,
                    "required", betAmount
                ));
            }
            
            // Roll the dice (1-6)
            int diceResult = random.nextInt(6) + 1;
            
            // Win if roll is 4, 5, or 6
            boolean won = diceResult >= 4;
            
            // Calculate winnings
            int winAmount = won ? betAmount * 2 : 0;
            int newBalance = currentBalance - betAmount + winAmount;
            int profit = winAmount - betAmount;
            
            // Update user balance
            UserProfile updatedUser = new UserProfile();
            updatedUser.setObrobucks(newBalance);
            userProfileService.updateUser(userId, updatedUser);
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("diceResult", diceResult);
            response.put("won", won);
            response.put("betAmount", betAmount);
            response.put("winAmount", winAmount);
            response.put("profit", profit);
            response.put("newBalance", newBalance);
            
            System.out.println("Dice roll result: " + response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in dice roll: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to play dice roll: " + e.getMessage()));
        }
    }

    /**
     * Play slot machine game
     * POST /api/gambling/slot-machine
     */
    @PostMapping("/slot-machine")
    public ResponseEntity<Map<String, Object>> slotMachine(@RequestBody Map<String, Object> request) {
        try {
            String userIdStr = (String) request.get("userId");
            UUID userId = UUID.fromString(userIdStr);
            int betAmount = ((Number) request.get("betAmount")).intValue();
            
            System.out.println("Slot machine for user: " + userId + ", bet: " + betAmount);
            
            // Get current user
            Optional<UserProfile> userOpt = userProfileService.getUserById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            UserProfile user = userOpt.get();
            int currentBalance = user.getObrobucks() != null ? user.getObrobucks() : 0;
            
            // Check if user has enough funds
            if (currentBalance < betAmount) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Insufficient funds",
                    "currentBalance", currentBalance,
                    "required", betAmount
                ));
            }
            
            // Slot symbols: 🍒 🍋 🍊 💎 7️⃣
            String[] symbols = {"cherry", "lemon", "orange", "diamond", "seven"};
            
            // Spin the slots (3 reels)
            String reel1 = symbols[random.nextInt(symbols.length)];
            String reel2 = symbols[random.nextInt(symbols.length)];
            String reel3 = symbols[random.nextInt(symbols.length)];
            
            // Calculate winnings
            int multiplier = 0;
            String resultType = "loss";
            
            if (reel1.equals(reel2) && reel2.equals(reel3)) {
                // All 3 match
                if (reel1.equals("seven")) {
                    multiplier = 10; // Jackpot!
                    resultType = "jackpot";
                } else if (reel1.equals("diamond")) {
                    multiplier = 7;
                    resultType = "big_win";
                } else {
                    multiplier = 5;
                    resultType = "triple_match";
                }
            } else if (reel1.equals(reel2) || reel2.equals(reel3) || reel1.equals(reel3)) {
                // 2 match
                multiplier = 2;
                resultType = "double_match";
            }
            // else: no match, multiplier stays 0
            
            int winAmount = betAmount * multiplier;
            int newBalance = currentBalance - betAmount + winAmount;
            int profit = winAmount - betAmount;
            
            // Update user balance
            UserProfile updatedUser = new UserProfile();
            updatedUser.setObrobucks(newBalance);
            userProfileService.updateUser(userId, updatedUser);
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reels", new String[]{reel1, reel2, reel3});
            response.put("resultType", resultType);
            response.put("multiplier", multiplier);
            response.put("betAmount", betAmount);
            response.put("winAmount", winAmount);
            response.put("profit", profit);
            response.put("newBalance", newBalance);
            
            System.out.println("Slot machine result: " + response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in slot machine: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to play slot machine: " + e.getMessage()));
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