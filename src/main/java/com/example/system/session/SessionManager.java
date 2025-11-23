package com.example.system.session;

import org.springframework.stereotype.Component;

import com.example.system.dtos.LoginResponse;

@Component
public class SessionManager {
    
    private static LoginResponse currentUser;
    
    public void setCurrentUser(LoginResponse user) {
        currentUser = user;
        System.out.println("Session set for user: " + (user != null ? user.getUsername() : "null"));
    }
    
    public LoginResponse getCurrentUser() {
        return currentUser;
    }
    
    public void clearSession() {
        currentUser = null;
        System.out.println("Session cleared");
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}