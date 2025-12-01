package com.example.rest_service.controller;

import com.example.rest_service.dto.CreatePollRequest;
import com.example.rest_service.model.Poll;
import com.example.rest_service.model.PollStatus;
import com.example.rest_service.service.PollSupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollSupabaseService service;

    public PollController(PollSupabaseService service) {
        this.service = service;
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
        if (req == null || req.question == null || req.question.isBlank() || req.options == null || req.options.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        PollStatus status = null;
        if (req.status != null) status = PollStatus.valueOf(req.status);
        UUID createdBy = UUID.fromString("9d947e28-5c8d-4dae-98a7-b2f0132d11c5"); //temp hardcode for testing
        Poll created = service.create(req.question, req.options, req.category, req.endsAt, status, createdBy);
        return ResponseEntity.created(URI.create("/api/polls/" + created.getId())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        boolean ok = service.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}