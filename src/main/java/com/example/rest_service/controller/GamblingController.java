package com.example.rest_service.controller;

import com.example.rest_service.service.UserProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gambling")
public class GamblingController {

    private final UserProfileService userProfileService;

    @Autowired
    public GamblingController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/open-lootbox")
    public Map<String, Object> openLootbox(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        int cost = (int) request.get("cost");
        int winAmount = (int) request.get("winAmount");
        
        // Get current user balance
        int currentBalance = userProfileService.getUserBalance(userId);
        
        // Check if user has enough
        if (currentBalance < cost) {
            throw new RuntimeException("Insufficient funds");
        }
        
        // Calculate new balance
        int newBalance = currentBalance - cost + winAmount;
        
        // Update balance in database
        userProfileService.updateUserBalance(userId, newBalance);
        
        Map<String, Object> response = new HashMap<>();
        response.put("newBalance", newBalance);
        response.put("profit", winAmount - cost);
        
        return response;
    }
}