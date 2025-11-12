package com.example.rest_service;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.net.URI;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin // you can remove this if you configure global CORS
@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollRepository repo;

    public PollController(PollRepository repo) {
        this.repo = repo;
    }

     @PostMapping
    public ResponseEntity<Poll> create(@RequestBody Poll body) {
        // defaults
        if (body.getStatus() == null) body.setStatus(PollStatus.PENDING);
        if (body.getCreatedAt() == null) body.setCreatedAt(LocalDateTime.now());

        // basic validation
        if (body.getQuestion() == null || body.getQuestion().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (body.getOptions() == null || body.getOptions().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Poll saved = repo.save(body);
        return ResponseEntity.created(URI.create("/api/polls/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Poll> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Poll> one(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Poll> byStatus(@PathVariable PollStatus status) {
        return repo.findByStatus(status);
    }

    // Optional: simple update endpoint (e.g., approve/close a poll)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Poll> updateStatus(@PathVariable Long id, @RequestParam PollStatus status) {
        return repo.findById(id).map(p -> {
            p.setStatus(status);
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}