package com.example.rest_service;

import jakarta.annotation.PostConstruct;               // 👈 add this import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseAnonKey;

    @Value("${supabase.service-role-key:}")
    private String supabaseServiceRoleKey;

    @PostConstruct
    public void init() {
        // 👇 Hard fallback if property is blank or not set
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            supabaseUrl = "https://vyxaqysujjmhjsaonszw.supabase.co";
        }
        System.out.println(">>> Supabase URL in SupabaseConfig.init(): " + supabaseUrl);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

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
