package com.example.rest_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bet {
    private Long id;
    
    @JsonProperty("poll_id")
    private Long pollId;
    
    @JsonProperty("user_id")
    private UUID userId;
    
    @JsonProperty("option_text")
    private String optionText;
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("potential_payout")
    private Integer potentialPayout;
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    
    @JsonProperty("is_winner")
    private Boolean isWinner;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPollId() { return pollId; }
    public void setPollId(Long pollId) { this.pollId = pollId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Integer getPotentialPayout() { return potentialPayout; }
    public void setPotentialPayout(Integer potentialPayout) { this.potentialPayout = potentialPayout; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsWinner() { return isWinner; }
    public void setIsWinner(Boolean isWinner) { this.isWinner = isWinner; }
}