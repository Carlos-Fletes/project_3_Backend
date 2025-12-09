package com.example.rest_service.controller;

import com.example.rest_service.model.Bet;
import com.example.rest_service.model.Poll;
import com.example.rest_service.model.UserProfile;
import com.example.rest_service.service.PollSupabaseService;
import com.example.rest_service.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import com.example.rest_service.SupabaseConfig;

import java.time.OffsetDateTime;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/betting")
public class BettingController {

    private final PollSupabaseService pollService;
    private final UserProfileService userProfileService;
    private final RestTemplate restTemplate;
    private final SupabaseConfig supabase;

    @Autowired
    public BettingController(
        PollSupabaseService pollService,
        UserProfileService userProfileService,
        RestTemplate restTemplate,
        SupabaseConfig supabase
    ) {
        this.pollService = pollService;
        this.userProfileService = userProfileService;
        this.restTemplate = restTemplate;
        this.supabase = supabase;
    }

    private String base(String table) {
        return supabase.getSupabaseUrl() + "/rest/v1/" + table;
    }

    private HttpHeaders writeHeaders() {
        HttpHeaders h = supabase.createSupabaseHeaders();
        h.set("Prefer", "return=representation");
        return h;
    }

    private HttpHeaders readHeaders() {
        return supabase.createSupabaseHeaders();
    }

    /**
     * Get betting stats for a specific poll
     * GET /api/betting/stats/{pollId}
     */
    @GetMapping("/stats/{pollId}")
    public ResponseEntity<Map<String, Object>> getPollStats(@PathVariable Long pollId) {
        try {
            // Get all bets for this poll
            String url = base("bets") + "?poll_id=eq." + pollId + "&select=option_text,amount";
            ResponseEntity<List> resp = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                new HttpEntity<>(readHeaders()), 
                List.class
            );

            List<Map<String, Object>> bets = resp.getBody();
            if (bets == null) bets = new ArrayList<>();

            // Calculate stats per option
            Map<String, Integer> totalPerOption = new HashMap<>();
            int grandTotal = 0;

            for (Map<String, Object> bet : bets) {
                String option = (String) bet.get("option_text");
                Integer amount = ((Number) bet.get("amount")).intValue();
                totalPerOption.put(option, totalPerOption.getOrDefault(option, 0) + amount);
                grandTotal += amount;
            }

            // Calculate percentages and odds
            Map<String, Map<String, Object>> optionStats = new HashMap<>();
            for (Map.Entry<String, Integer> entry : totalPerOption.entrySet()) {
                String option = entry.getKey();
                int optionTotal = entry.getValue();
                
                double percentage = grandTotal > 0 ? (optionTotal * 100.0 / grandTotal) : 0;
                double odds = optionTotal > 0 ? (double) grandTotal / optionTotal : 2.0;
                
                Map<String, Object> stats = new HashMap<>();
                stats.put("total", optionTotal);
                stats.put("percentage", Math.round(percentage * 10) / 10.0);
                stats.put("odds", Math.round(odds * 100) / 100.0);
                
                optionStats.put(option, stats);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("pollId", pollId);
            response.put("grandTotal", grandTotal);
            response.put("optionStats", optionStats);
            response.put("betCount", bets.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting poll stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get poll stats"));
        }
    }

    /**
     * Place a bet on a poll option
     * POST /api/betting/place
     */
    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeBet(@RequestBody Map<String, Object> request) {
        try {
            // Extract request data
            String userIdStr = (String) request.get("userId");
            UUID userId = UUID.fromString(userIdStr);
            Long pollId = ((Number) request.get("pollId")).longValue();
            String optionText = (String) request.get("optionText");
            int betAmount = ((Number) request.get("amount")).intValue();

            System.out.println("Place bet: user=" + userId + ", poll=" + pollId + 
                             ", option=" + optionText + ", amount=" + betAmount);

            // Validate poll exists
            Optional<Poll> pollOpt = pollService.get(pollId);
            if (!pollOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Poll not found"));
            }

            Poll poll = pollOpt.get();

            // Validate option exists
            if (!poll.getOptions().contains(optionText)) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid option"));
            }

            // Get user
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

            // Get current stats to calculate odds
            ResponseEntity<Map<String, Object>> statsResp = getPollStats(pollId);
            Map<String, Object> stats = statsResp.getBody();
            int currentTotal = (int) stats.get("grandTotal");
            
            // Calculate potential payout (simplified odds: total pool / option pool)
            // After this bet, the total will be currentTotal + betAmount
            int newTotal = currentTotal + betAmount;
            
            // Assume this option gets betAmount, calculate payout
            // Odds = newTotal / (amount bet on this option)
            // For simplicity, we'll use 2x odds (can be refined)
            int potentialPayout = betAmount * 2;

            // Deduct bet amount from user balance
            int newBalance = currentBalance - betAmount;
            UserProfile updatedUser = new UserProfile();
            updatedUser.setObrobucks(newBalance);
            userProfileService.updateUser(userId, updatedUser);

            // Create bet record
            Map<String, Object> betData = new HashMap<>();
            betData.put("poll_id", pollId);
            betData.put("user_id", userId);
            betData.put("option_text", optionText);
            betData.put("amount", betAmount);
            betData.put("potential_payout", potentialPayout);
            betData.put("created_at", OffsetDateTime.now().toString());
            betData.put("is_winner", null);

            HttpEntity<Object> entity = new HttpEntity<>(betData, writeHeaders());
            ResponseEntity<Bet[]> insertResp = restTemplate.exchange(
                base("bets"),
                HttpMethod.POST,
                entity,
                Bet[].class
            );

            // Update poll total_bets
            int newPollTotal = currentTotal + betAmount;
            Map<String, Object> pollUpdate = new HashMap<>();
            pollUpdate.put("total_bets", newPollTotal);
            
            HttpEntity<Object> pollEntity = new HttpEntity<>(pollUpdate, writeHeaders());
            restTemplate.exchange(
                base("polls") + "?id=eq." + pollId,
                HttpMethod.PATCH,
                pollEntity,
                String.class
            );

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("betAmount", betAmount);
            response.put("potentialPayout", potentialPayout);
            response.put("newBalance", newBalance);
            response.put("option", optionText);

            System.out.println("Bet placed successfully: " + response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error placing bet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to place bet: " + e.getMessage()));
        }
    }

    /**
     * Get user's bets for a specific poll
     * GET /api/betting/user/{userId}/poll/{pollId}
     */
    @GetMapping("/user/{userId}/poll/{pollId}")
    public ResponseEntity<List<Bet>> getUserBetsForPoll(
        @PathVariable String userId,
        @PathVariable Long pollId
    ) {
        try {
            String url = base("bets") + 
                        "?user_id=eq." + userId + 
                        "&poll_id=eq." + pollId +
                        "&select=*";
            
            ResponseEntity<Bet[]> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(readHeaders()),
                Bet[].class
            );

            List<Bet> bets = Arrays.asList(
                Optional.ofNullable(resp.getBody()).orElse(new Bet[0])
            );

            return ResponseEntity.ok(bets);

        } catch (Exception e) {
            System.err.println("Error getting user bets: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}