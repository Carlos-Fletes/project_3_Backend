package com.example.rest_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@SpringBootTest
class RestServiceApplicationTests {

    @MockBean
    private SupabaseService supabaseService;

    @Test
    void contextLoads() {
        // just check that the context starts
    }

    @Configuration
    static class TestSecurityConfig {
        @Bean
        public AuthenticationSuccessHandler successHandler() {
            // Provide a simple stub or mock handler
            return (request, response, authentication) -> {
                // do nothing, just satisfy the dependency
            };
        }
    }
}
