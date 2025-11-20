package com.example.rest_service.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class CreatePollRequest {
    public String question;
    public List<String> options;
    public String category;
    public OffsetDateTime endsAt;

    public String status;
}