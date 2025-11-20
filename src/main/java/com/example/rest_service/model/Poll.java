package com.example.rest_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Poll {
    private Long id;
    private String question;
    private PollStatus status;
    private String category;
    private Integer total_bets;
    private OffsetDateTime created_at;
    private OffsetDateTime ends_at;

    private List<String> options = new ArrayList<>();

// Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public PollStatus getStatus() { return status; }
    public void setStatus(PollStatus status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTotal_bets() { return total_bets; }
    public void setTotal_bets(Integer total_bets) { this.total_bets = total_bets; }

    public OffsetDateTime getCreated_at() { return created_at; }
    public void setCreated_at(OffsetDateTime created_at) { this.created_at = created_at; }

    public OffsetDateTime getEnds_at() { return ends_at; }
    public void setEnds_at(OffsetDateTime ends_at) { this.ends_at = ends_at; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
