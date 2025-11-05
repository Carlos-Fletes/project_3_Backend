package com.example.rest_service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class OAuthController {

    // In-memory storage for demo purposes
    private String accessToken = null;
    private String refreshToken = null;
    private String status = "idle";

    private final String clientId = "859878511345-10q6cd9telk3anbd7kf8vgh0reqt7pid.apps.googleusercontent.com";
    private final String clientSecret = "<YOUR_GOOGLE_CLIENT_SECRET>";
    private final String redirectUri = "https://betsocial-fde6ef886274.herokuapp.com/login/oauth2/code/google";

    @GetMapping("/start")
    public void startOAuth(HttpServletResponse response) throws IOException {
        status = "waiting";

        // Build Google OAuth URL
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=openid%20email%20profile";

        response.sendRedirect(url);
    }

    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", status);
        if ("success".equals(status)) {
            res.put("accessToken", accessToken);
            res.put("refreshToken", refreshToken);
        }
        return res;
    }

    @GetMapping("/login/oauth2/code/google")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        try {
            // Exchange the authorization code for tokens
            RestTemplate restTemplate = new RestTemplate();
            String tokenUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "code=" + code +
                          "&client_id=" + clientId +
                          "&client_secret=" + clientSecret +
                          "&redirect_uri=" + redirectUri +
                          "&grant_type=authorization_code";

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> tokenMap = tokenResponse.getBody();

            if (tokenMap != null) {
                accessToken = (String) tokenMap.get("access_token");
                refreshToken = (String) tokenMap.get("refresh_token");
                status = "success";
            } else {
                status = "error";
            }

            response.sendRedirect("/success"); // optional frontend redirect
        } catch (Exception e) {
            e.printStackTrace();
            status = "error";
            response.sendRedirect("/error");
        }
    }
}
