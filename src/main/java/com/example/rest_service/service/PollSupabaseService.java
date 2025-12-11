package com.example.rest_service.service;

import com.example.rest_service.SupabaseConfig;
import com.example.rest_service.model.Poll;
import com.example.rest_service.model.PollStatus;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PollSupabaseService {

    private final RestTemplate restTemplate;
    private final SupabaseConfig supabase;

    public PollSupabaseService(RestTemplate restTemplate, SupabaseConfig supabase) {
        this.restTemplate = restTemplate;
        this.supabase = supabase;
    }

    private HttpHeaders readHeaders() {
        return supabase.createSupabaseHeaders();
    }

    private HttpHeaders writeHeaders() {
        HttpHeaders h = supabase.createSupabaseHeaders();
        h.set("Prefer", "return=representation");
        return h;
    }

    private String base(String table) {
        return supabase.getSupabaseUrl() + "/rest/v1/" + table;
    }

    private List<String> fetchOptions(long pollId) {
        String url = base("poll_options") + "?poll_id=eq." + pollId + "&select=option_text";
        ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(readHeaders()), List.class);
        List<Map<String,Object>> rows = resp.getBody();
        if (rows == null) return List.of();
        return rows.stream().map(m -> String.valueOf(m.get("option_text"))).collect(Collectors.toList());
    }

    private Map<Long, List<String>> fetchOptionsForIds(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        String in = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        String url = base("poll_options") + "?poll_id=in.(" + in + ")&select=poll_id,option_text";
        ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(readHeaders()), List.class);
        Map<Long, List<String>> out = new HashMap<>();
        List<Map<String,Object>> rows = resp.getBody();
        if (rows != null) {
            for (Map<String,Object> r : rows) {
                Long pid = ((Number) r.get("poll_id")).longValue();
                String txt = String.valueOf(r.get("option_text"));
                out.computeIfAbsent(pid, k -> new ArrayList<>()).add(txt);
            }
        }
        return out;
    }

    // NEW METHOD: Fetch usernames for poll creators
    private Map<String, String> fetchUsernames(List<String> userIds) {
        if (userIds.isEmpty()) return Map.of();
        
        String in = userIds.stream()
            .map(id -> "\"" + id + "\"")
            .collect(Collectors.joining(","));
        
        String url = base("user_profiles") + "?id=in.(" + in + ")&select=id,username";
        
        try {
            ResponseEntity<List> resp = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                new HttpEntity<>(readHeaders()), 
                List.class
            );
            
            Map<String, String> usernameMap = new HashMap<>();
            List<Map<String, Object>> rows = resp.getBody();
            
            if (rows != null) {
                for (Map<String, Object> r : rows) {
                    String userId = String.valueOf(r.get("id"));
                    String username = String.valueOf(r.get("username"));
                    usernameMap.put(userId, username);
                }
            }
            
            return usernameMap;
        } catch (Exception e) {
            System.err.println("Error fetching usernames: " + e.getMessage());
            return Map.of();
        }
    }

    public List<Poll> list() {
        String url = base("polls")
            + "?select=id,question,status,category,total_bets,created_at,ends_at,created_by"
            + "&order=created_at.desc";

        ResponseEntity<Poll[]> resp =
            restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(readHeaders()), Poll[].class);

        List<Poll> polls = Arrays.asList(
            Optional.ofNullable(resp.getBody()).orElse(new Poll[0])
        );

        // Fetch options
        Map<Long, List<String>> options =
            fetchOptionsForIds(polls.stream().map(Poll::getId).filter(Objects::nonNull).toList());

        // Fetch usernames for creators
        List<String> creatorIds = polls.stream()
            .map(Poll::getCreatedBy)
            .filter(Objects::nonNull)
            .map(UUID::toString)
            .distinct()
            .collect(Collectors.toList());
        
        Map<String, String> usernames = fetchUsernames(creatorIds);

        // Populate polls with options and usernames
        for (Poll p : polls) {
            p.setOptions(options.getOrDefault(p.getId(), List.of()));
            
            if (p.getCreatedBy() != null) {
                String username = usernames.get(p.getCreatedBy().toString());
                if (username != null && !username.equals("null")) {
                    p.setCreatedByUsername(username);
                }
            }
        }
        
        return polls;
    }

    public Optional<Poll> get(long id) {
        String url = base("polls")
            + "?id=eq." + id
            + "&select=id,question,status,category,total_bets,created_at,ends_at,created_by";

        ResponseEntity<Poll[]> resp =
            restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(readHeaders()), Poll[].class);

        Poll[] arr = resp.getBody();
        if (arr == null || arr.length == 0) return Optional.empty();

        Poll p = arr[0];
        p.setOptions(fetchOptions(id));
        
        // Fetch username for creator
        if (p.getCreatedBy() != null) {
            Map<String, String> usernames = fetchUsernames(List.of(p.getCreatedBy().toString()));
            String username = usernames.get(p.getCreatedBy().toString());
            if (username != null && !username.equals("null")) {
                p.setCreatedByUsername(username);
            }
        }
        
        return Optional.of(p);
    }

    public Poll create(String question,
                       List<String> options,
                       String category,
                       OffsetDateTime endsAt,
                       PollStatus status,
                       UUID createdBy) {

        if (status == null) status = PollStatus.PENDING;
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("options required");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("createdBy (user id) is required");
        }

        Map<String,Object> payload = new HashMap<>();
        payload.put("question", question);
        payload.put("status", status.name());
        payload.put("category", category);
        payload.put("total_bets", 0);
        payload.put("created_at", OffsetDateTime.now().toString());
        if (endsAt != null) {
            payload.put("ends_at", endsAt.toString());
        }
        payload.put("created_by", createdBy);

        HttpEntity<Object> entity = new HttpEntity<>(payload, writeHeaders());

        ResponseEntity<Poll[]> insertResp = restTemplate.exchange(
                base("polls"),
                HttpMethod.POST,
                entity,
                Poll[].class
        );

        Poll[] inserted = insertResp.getBody();
        if (inserted == null || inserted.length == 0 || inserted[0].getId() == null) {
            throw new RuntimeException("Failed to insert poll");
        }

        long pollId = inserted[0].getId();

        try {
            // insert poll options
            List<Map<String,Object>> optionRows = options.stream().map(txt -> {
                Map<String,Object> row = new HashMap<>();
                row.put("poll_id", pollId);
                row.put("option_text", txt);
                return row;
            }).toList();

            HttpEntity<Object> optEntity = new HttpEntity<>(optionRows, writeHeaders());
            restTemplate.exchange(base("poll_options"), HttpMethod.POST, optEntity, String.class);

            Poll created = inserted[0];
            created.setOptions(new ArrayList<>(options));
            
            // Fetch username for creator
            if (created.getCreatedBy() != null) {
                Map<String, String> usernames = fetchUsernames(List.of(created.getCreatedBy().toString()));
                String username = usernames.get(created.getCreatedBy().toString());
                if (username != null && !username.equals("null")) {
                    created.setCreatedByUsername(username);
                }
            }
            
            return created;

        } catch (Exception e) {
            try {
                restTemplate.exchange(
                        base("polls") + "?id=eq." + pollId,
                        HttpMethod.DELETE,
                        new HttpEntity<>(readHeaders()),
                        String.class
                );
            } catch (Exception ignored) {}
            throw e;
        }
    }

    public boolean delete(long id) {
        // delete options first
        restTemplate.exchange(
                base("poll_options") + "?poll_id=eq." + id,
                HttpMethod.DELETE,
                new HttpEntity<>(readHeaders()),
                String.class
        );

        ResponseEntity<String> resp = restTemplate.exchange(
                base("polls") + "?id=eq." + id,
                HttpMethod.DELETE,
                new HttpEntity<>(readHeaders()),
                String.class
        );
        return resp.getStatusCode().is2xxSuccessful();
    }
}