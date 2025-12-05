package com.enrollment.system.controller;

import com.enrollment.system.dto.LoginRequest;
import com.enrollment.system.dto.LoginResponse;
import com.enrollment.system.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private String sessionToken;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        
        // Add enter key listener to password field
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Show loading state
        setLoading(true);
        hideError();

        // Perform login in background thread
        new Thread(() -> {
            try {
                LoginRequest loginRequest = new LoginRequest(username, password);
                LoginResponse response = authService.login(loginRequest);
                
                if (response.isSuccess()) {
                    sessionToken = response.getSessionToken();
                    Platform.runLater(() -> {
                        setLoading(false);
                        showSuccessAndNavigate(response);
                    });
                } else {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError(response.getMessage());
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Login failed: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showSuccessAndNavigate(LoginResponse response) {
        try {
            // Show welcome message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Successful");
            alert.setHeaderText("Welcome, " + response.getUser().getFullName() + "!");
            alert.setContentText("Role: " + response.getUser().getRoleDisplayName());
            alert.showAndWait();
            
            // Navigate to dashboard
            loadDashboard(response);
            
        } catch (Exception e) {
            showError("Error loading dashboard");
            e.printStackTrace();
        }
    }
    
    private void loadDashboard(LoginResponse response) {
        try {
            // Load Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Dashboard.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            
            // Load the FXML - this will call initialize()
            Parent dashboardRoot = loader.load();
            
            // Get dashboard controller and set user session
            DashboardController dashboardController = loader.getController();
            if (dashboardController != null) {
                dashboardController.setUserSession(response.getUser(), response.getSessionToken());
            }
            
            // Create new stage for dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Seguinon SHS Enrollment System - Dashboard");
            dashboardStage.setScene(new Scene(dashboardRoot));
            dashboardStage.setMaximized(true);
            
            // Close login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
            
            // Show dashboard
            dashboardStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
            // Show full stack trace for debugging
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Dashboard Loading Error");
            errorAlert.setHeaderText("Failed to load dashboard");
            errorAlert.setContentText("Error: " + e.getMessage() + "\n\nCheck console for details.");
            errorAlert.showAndWait();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
    
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
    }
    
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Reset");
        alert.setContentText("Please contact the system administrator to reset your password.");
        alert.showAndWait();
    }
}