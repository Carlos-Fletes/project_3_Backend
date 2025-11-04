package com.example.rest_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@SpringBootTest
class RestServiceApplicationTests {

    // Mock the beans Spring can't create in the test context
    @MockBean
    private AuthenticationSuccessHandler successHandler;

    @MockBean
    private SupabaseService supabaseService;

    @Test
    void contextLoads() {
    }
}
