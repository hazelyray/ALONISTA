package com.enrollment.system.controller;

import com.enrollment.system.dto.LoginRequest;
import com.enrollment.system.dto.LoginResponse;
import com.enrollment.system.dto.UserDto;
import com.enrollment.system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String sessionToken) {
        boolean success = authService.logout(sessionToken);
        
        if (success) {
            return ResponseEntity.ok("Logged out successfully");
        } else {
            return ResponseEntity.badRequest().body("Logout failed");
        }
    }
    
    @GetMapping("/current-user")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String sessionToken) {
        UserDto user = authService.getCurrentUser(sessionToken);
        
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).build();
        }
    }
    
    @GetMapping("/validate-session")
    public ResponseEntity<Boolean> validateSession(@RequestHeader("Authorization") String sessionToken) {
        boolean isValid = authService.isSessionValid(sessionToken);
        return ResponseEntity.ok(isValid);
    }
}