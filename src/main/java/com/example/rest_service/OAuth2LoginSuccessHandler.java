package com.example.rest_service;

import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Generate JWT
        String jwt = JwtUtil.generateToken(authentication.getName());

        // Redirect to Expo deep link
        String redirectUrl = "project3fe://auth?token=" + jwt;
        new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
