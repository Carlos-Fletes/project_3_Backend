package com.example.rest_service.controller;

import com.example.rest_service.dto.CreatePollRequest;
import com.example.rest_service.model.Poll;
import com.example.rest_service.model.PollStatus;
import com.example.rest_service.model.Bet;
import com.example.rest_service.model.UserProfile;
import com.example.rest_service.service.PollSupabaseService;
import com.example.rest_service.service.UserProfileService;
import com.example.rest_service.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollSupabaseService service;
    private final UserProfileService userProfileService;
    private final RestTemplate restTemplate;
    private final SupabaseConfig supabase;

    @Autowired
    public PollController(
        PollSupabaseService service,
        UserProfileService userProfileService,
        RestTemplate restTemplate,
        SupabaseConfig supabase
    ) {
        this.service = service;
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

    @GetMapping
    public List<Poll> all() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Poll> one(@PathVariable long id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Poll> create(@RequestBody CreatePollRequest req) {

        if (req == null ||
            req.question == null || req.question.isBlank() ||
            req.options == null || req.options.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        PollStatus status = null;
        if (req.status != null) {
            status = PollStatus.valueOf(req.status);
        }

        // Use the createdBy from request, or fallback to hardcoded for testing
        UUID createdBy = req.createdBy != null 
            ? req.createdBy 
            : UUID.fromString("9d947e28-5c8d-4dae-98a7-b2f0132d11c5");

        Poll created = service.create(
                req.question,
                req.options,
                req.category,
                req.endsAt,
                status,
                createdBy
        );

        return ResponseEntity
                .created(URI.create("/api/polls/" + created.getId()))
                .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        boolean ok = service.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Resolve a poll - set winning option and pay out winners
     * POST /api/polls/{id}/resolve
     * Body: { "winningOption": "Yes" }
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolvePoll(
        @PathVariable long id,
        @RequestBody Map<String, String> request
    ) {
        try {
            String winningOption = request.get("winningOption");
            
            if (winningOption == null || winningOption.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "winningOption is required"));
            }

            // Get the poll
            Optional<Poll> pollOpt = service.get(id);
            if (!pollOpt.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Poll not found"));
            }

            Poll poll = pollOpt.get();

            // Validate winning option exists
            if (!poll.getOptions().contains(winningOption)) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Invalid winning option"));
            }

            // Check if already closed
            if (poll.getStatus() == PollStatus.CLOSED) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Poll already closed"));
            }

            // Get all bets for this poll
            String betsUrl = base("bets") + "?poll_id=eq." + id + "&select=*";
            ResponseEntity<Bet[]> betsResp = restTemplate.exchange(
                betsUrl,
                HttpMethod.GET,
                new HttpEntity<>(readHeaders()),
                Bet[].class
            );

            List<Bet> bets = Arrays.asList(
                Optional.ofNullable(betsResp.getBody()).orElse(new Bet[0])
            );

            int winnersCount = 0;
            int totalPaidOut = 0;

            // Process each bet
            for (Bet bet : bets) {
                boolean isWinner = bet.getOptionText().equals(winningOption);
                
                // Update bet record
                Map<String, Object> betUpdate = new HashMap<>();
                betUpdate.put("is_winner", isWinner);

                HttpEntity<Object> betEntity = new HttpEntity<>(betUpdate, writeHeaders());
                restTemplate.exchange(
                    base("bets") + "?id=eq." + bet.getId(),
                    HttpMethod.PATCH,
                    betEntity,
                    String.class
                );

                // Pay winners
                if (isWinner && bet.getPotentialPayout() != null) {
                    Optional<UserProfile> userOpt = userProfileService.getUserById(bet.getUserId());
                    if (userOpt.isPresent()) {
                        UserProfile user = userOpt.get();
                        int currentBalance = user.getObrobucks() != null ? user.getObrobucks() : 0;
                        int newBalance = currentBalance + bet.getPotentialPayout();

                        UserProfile updatedUser = new UserProfile();
                        updatedUser.setObrobucks(newBalance);
                        userProfileService.updateUser(bet.getUserId(), updatedUser);

                        winnersCount++;
                        totalPaidOut += bet.getPotentialPayout();
                    }
                }
            }

            // Update poll status to CLOSED
            Map<String, Object> pollUpdate = new HashMap<>();
            pollUpdate.put("status", PollStatus.CLOSED.name());
            
            HttpEntity<Object> pollEntity = new HttpEntity<>(pollUpdate, writeHeaders());
            restTemplate.exchange(
                base("polls") + "?id=eq." + id,
                HttpMethod.PATCH,
                pollEntity,
                String.class
            );

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pollId", id);
            response.put("winningOption", winningOption);
            response.put("winnersCount", winnersCount);
            response.put("totalPaidOut", totalPaidOut);
            response.put("totalBets", bets.size());

            System.out.println("Poll resolved successfully: " + response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error resolving poll: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to resolve poll: " + e.getMessage()));
        }
    }

    /**
     * Get poll winner (if resolved)
     * GET /api/polls/{id}/winner
     */
    @GetMapping("/{id}/winner")
    public ResponseEntity<Map<String, Object>> getWinner(@PathVariable long id) {
        try {
            Optional<Poll> pollOpt = service.get(id);
            if (!pollOpt.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Poll not found"));
            }

            Poll poll = pollOpt.get();
            
            if (poll.getStatus() != PollStatus.CLOSED) {
                return ResponseEntity.ok(Map.of("resolved", false));
            }

            // Get a winning bet to find the winning option
            String betsUrl = base("bets") + "?poll_id=eq." + id + "&is_winner=eq.true&limit=1";
            ResponseEntity<Bet[]> betsResp = restTemplate.exchange(
                betsUrl,
                HttpMethod.GET,
                new HttpEntity<>(readHeaders()),
                Bet[].class
            );

            Bet[] winningBets = betsResp.getBody();
            if (winningBets != null && winningBets.length > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("resolved", true);
                response.put("winningOption", winningBets[0].getOptionText());
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(Map.of("resolved", false));

        } catch (Exception e) {
            System.err.println("Error getting winner: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to get winner"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<com.example.rest_service.model.Poll>> search(
            @RequestParam(name="q", required=false) String q,
            @RequestParam(name="category", required=false) String category) {
        try {
            List<com.example.rest_service.model.Poll> results = service.search(q, category);
            return ResponseEntity.ok(results);
        } 
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}