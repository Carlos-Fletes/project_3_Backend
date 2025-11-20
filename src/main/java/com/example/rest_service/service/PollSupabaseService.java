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


    public List<Poll> list() {
        String url = base("polls") + "?select=id,question,status,category,total_bets,created_at,ends_at&order=created_at.desc";
        ResponseEntity<Poll[]> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(readHeaders()), Poll[].class);
        List<Poll> polls = Arrays.asList(Optional.ofNullable(resp.getBody()).orElse(new Poll[0]));
        Map<Long, List<String>> options = fetchOptionsForIds(polls.stream().map(Poll::getId).filter(Objects::nonNull).toList());
        for (Poll p : polls) p.setOptions(options.getOrDefault(p.getId(), List.of()));
        return polls;
    }

    public Optional<Poll> get(long id) {
        String url = base("polls") + "?id=eq." + id + "&select=id,question,status,category,total_bets,created_at,ends_at";
        ResponseEntity<Poll[]> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(readHeaders()), Poll[].class);
        Poll[] arr = resp.getBody();
        if (arr == null || arr.length == 0) return Optional.empty();
        Poll p = arr[0];
        p.setOptions(fetchOptions(id));
        return Optional.of(p);
    }

    public Poll create(String question, List<String> options, String category, OffsetDateTime endsAt, PollStatus status) {
        if (status == null) status = PollStatus.PENDING;
        if (options == null || options.isEmpty()) throw new IllegalArgumentException("options required");

        Map<String,Object> payload = new HashMap<>();
        payload.put("question", question);
        payload.put("status", status.name());
        payload.put("category", category);
        payload.put("total_bets", 0);
        payload.put("created_at", OffsetDateTime.now());
        if (endsAt != null) payload.put("ends_at", endsAt);

        HttpEntity<Object> entity = new HttpEntity<>(payload, writeHeaders());
        ResponseEntity<Poll[]> insertResp = restTemplate.exchange(base("polls"), HttpMethod.POST, entity, Poll[].class);
        Poll[] inserted = insertResp.getBody();
        if (inserted == null || inserted.length == 0 || inserted[0].getId() == null) {
            throw new RuntimeException("Failed to insert poll");
        }
        long pollId = inserted[0].getId();

        try {
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
            return created;

        } catch (Exception e) {
            try {
                restTemplate.exchange(base("polls") + "?id=eq." + pollId, HttpMethod.DELETE, new HttpEntity<>(readHeaders()), String.class);
            } catch (Exception ignored) {}
            throw e;
        }
    }

    public boolean delete(long id) {
        restTemplate.exchange(base("poll_options") + "?poll_id=eq." + id, HttpMethod.DELETE, new HttpEntity<>(readHeaders()), String.class);
        ResponseEntity<String> resp = restTemplate.exchange(base("polls") + "?id=eq." + id, HttpMethod.DELETE, new HttpEntity<>(readHeaders()), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }
}