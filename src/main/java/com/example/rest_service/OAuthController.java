package com.example.rest_service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class OAuthController {

    @GetMapping("/login/oauth2/code/google")
    public void googleOAuthCallback(HttpServletResponse response) throws IOException {
        // Redirect to Expo app home screen
        response.sendRedirect("myapp://home");
    }
}
