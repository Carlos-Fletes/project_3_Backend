package com.example.rest_service;

import com.example.rest_service.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final SupabaseService supabaseService;
    private final UserProfileService userProfileService;

    @Autowired
    public HelloController(SupabaseService supabaseService, UserProfileService userProfileService) {
        this.supabaseService = supabaseService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/")  // This will respond to requests to the root URL
    public String hello() {
        return "Hello from Spring Boot + Supabase! User Profile API is ready at /api/users";
    }

    @GetMapping("/supabase/test")
    public String testSupabase() {
        return supabaseService.testConnection();
    }

    @GetMapping("/supabase/table/{tableName}")
    public String getTableData(@PathVariable String tableName) {
        return supabaseService.fetchFromTable(tableName);
    }

    @GetMapping("/test/users")
    public String testUserService() {
        try {
            int userCount = userProfileService.getAllUsers().size();
            return "User Profile Service is working! Found " + userCount + " users in the database.";
        } catch (Exception e) {
            return "User Profile Service error: " + e.getMessage();
        }
    }
}
