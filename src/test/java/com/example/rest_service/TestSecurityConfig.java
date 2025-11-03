package com.example.rest_service;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        // Simple stub for testing
        return (request, response, authentication) -> {};
    }
}
