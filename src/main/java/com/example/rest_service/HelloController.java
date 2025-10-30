package com.example.rest_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final SupabaseService supabaseService;

    @Autowired
    public HelloController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/")  // This will respond to requests to the root URL
    public String hello() {
        return "Hello from Spring Boot + Supabase!";
    }

    @GetMapping("/supabase/test")
    public String testSupabase() {
        return supabaseService.testConnection();
    }

    @GetMapping("/supabase/table/{tableName}")
    public String getTableData(@PathVariable String tableName) {
        return supabaseService.fetchFromTable(tableName);
    }
}
