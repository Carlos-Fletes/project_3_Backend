package com.example.rest_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@Service
public class SupabaseService {

    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;

    @Autowired
    public SupabaseService(RestTemplate restTemplate, SupabaseConfig supabaseConfig) {
        this.restTemplate = restTemplate;
        this.supabaseConfig = supabaseConfig;
    }

    /**
     * Test connection to Supabase by making a simple API call
     */
    public String testConnection() {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/";
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return "Supabase connection successful! Status: " + response.getStatusCode();
        } catch (Exception e) {
            return "Supabase connection failed: " + e.getMessage();
        }
    }

    /**
     * Generic method to fetch data from a Supabase table
     */
    public String fetchFromTable(String tableName) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/" + tableName;
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            return "Error fetching from table " + tableName + ": " + e.getMessage();
        }
    }

    /**
     * Generic method to insert data into a Supabase table
     */
    public String insertIntoTable(String tableName, Map<String, Object> data) {
        try {
            HttpHeaders headers = supabaseConfig.createSupabaseHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            
            String url = supabaseConfig.getSupabaseUrl() + "/rest/v1/" + tableName;
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            return "Error inserting into table " + tableName + ": " + e.getMessage();
        }
    }
}