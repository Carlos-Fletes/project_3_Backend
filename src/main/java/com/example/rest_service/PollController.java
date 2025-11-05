package com.example.rest_service;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@CrossOrigin
@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollRepository repo;
    private final SupabaseService supabaseService;

    public PollController(PollRepository repo, SupabaseService supabaseService) {
        this.repo = repo;
        this.supabaseService = supabaseService;
    }

    @PostMapping
    public ResponseEntity<Poll> create(@RequestBody Poll body) {
        if (body.getStatus() == null) body.setStatus(PollStatus.PENDING);

        try {
            // Build payload for polls table
            Map<String, Object> payload = new HashMap<>();
            payload.put("question", body.getQuestion());
            payload.put("status", body.getStatus().name());
            payload.put("category", body.getCategory());
            payload.put("total_bets", body.getTotalBets());
            payload.put("created_at", body.getCreatedAt() != null ? body.getCreatedAt().toString() : null);
            payload.put("ends_at", body.getEndsAt() != null ? body.getEndsAt().toString() : null);

            // Insert into Supabase polls table
            String insertResp = supabaseService.insertIntoTable("polls", payload);

            // Parse response to extract generated id (Supabase returns an array with inserted row)
            Long generatedId = null;
            if (insertResp != null && !insertResp.isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(insertResp);
                if (root.isArray() && root.size() > 0 && root.get(0).has("id")) {
                    generatedId = root.get(0).get("id").asLong();
                }
            }

            // Insert options into poll_options table
            if (generatedId != null && body.getOptions() != null) {
                for (String opt : body.getOptions()) {
                    Map<String, Object> optPayload = new HashMap<>();
                    optPayload.put("poll_id", generatedId);
                    optPayload.put("option_text", opt);
                    supabaseService.insertIntoTable("poll_options", optPayload);
                }
            }

            // Set id on returned object for client
            if (generatedId != null) body.setId(generatedId);

            return new ResponseEntity<>(body, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public List<Poll> all() {
        return repo.findAll(); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Poll> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Poll> byStatus(@PathVariable PollStatus status) {
        return repo.findByStatus(status);
    }
}