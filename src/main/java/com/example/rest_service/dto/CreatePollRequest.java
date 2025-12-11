package com.example.rest_service.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CreatePollRequest {
    public String question;
    public List<String> options;
    public String category;
    public OffsetDateTime endsAt;
    public String status;
    public UUID createdBy;
}