package com.example.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.system.dtos.LoginRequest;
import com.example.system.dtos.LoginResponse;
import com.example.system.services.AuthService;
import com.example.system.session.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private ApplicationContext springContext;
    
    @FXML
    public void initialize() {
        System.out.println("Login Controller initialized");
    }
    
    @FXML
    public void handleMouseEntered() {
        loginButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 12;");
    }
    
    @FXML
    public void handleMouseExited() {
        loginButton.setStyle("-fx-background-color: #1e88e5; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 12;");
    }
    
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password");
            return;
        }
        
        try {
            // Create login request
            LoginRequest loginRequest = new LoginRequest(username, password);
            
            // Call the backend authentication service
            LoginResponse response = authService.login(loginRequest);
            
            // Save to session
            sessionManager.setCurrentUser(response);
            
            System.out.println("Login successful for: " + response.getFullName());
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", 
                    "Welcome, " + response.getFullName() + "!");
            
            // Load homepage
            loadHomepage();
            
        } catch (RuntimeException e) {
            // Show error message
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred");
            e.printStackTrace();
        }
    }
    
    private void loadHomepage() {
        try {
            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Try to load from /fxml/ folder (lowercase)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Homepage.fxml"));
            loader.setControllerFactory(springContext::getBean);
            
            System.out.println("Loading Homepage from: " + getClass().getResource("/fxml/Homepage.fxml"));
            
            Parent root = loader.load();
            
            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("PaySTI - Home");
            stage.show();
            
            System.out.println("Homepage loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Could not load homepage");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load homepage: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}