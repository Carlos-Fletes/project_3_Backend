package com.example.rest_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseAnonKey;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Getters for accessing configuration values
    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseAnonKey() {
        return supabaseAnonKey;
    }

    public String getSupabaseServiceRoleKey() {
        return supabaseServiceRoleKey;
    }

    public HttpHeaders createSupabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.set("Authorization", "Bearer " + supabaseAnonKey);
        headers.set("Content-Type", "application/json");
        headers.set("Prefer", "return=representation");
        return headers;
    }
    
    public HttpHeaders createSupabaseHeadersForUpdate() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.set("Authorization", "Bearer " + supabaseAnonKey);
        headers.set("Content-Type", "application/json");
        headers.set("Prefer", "return=representation");
        headers.set("Content-Profile", "public");
        return headers;
    }
}