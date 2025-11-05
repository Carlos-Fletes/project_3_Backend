package com.example.rest_service;
import org.springframework.stereotype.Component;

@Component
public class OAuthState {
    private volatile boolean waiting = false;
    private volatile boolean success = false;
    private volatile String accessToken = null;
    private volatile String refreshToken = null;
    private volatile String error = null;

    // Getters and setters
    public boolean isWaiting() { return waiting; }
    public void setWaiting(boolean waiting) { this.waiting = waiting; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
