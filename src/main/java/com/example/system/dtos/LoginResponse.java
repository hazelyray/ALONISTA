package com.example.system.dtos;

public class LoginResponse {
    
    private String token;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String message;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String message) {
        this.message = message;
    }
    
    public LoginResponse(String token, String username, String fullName, String email, String role, String message) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.message = message;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}