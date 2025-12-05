package com.enrollment.system.controller;

import com.enrollment.system.dto.UserDto;
import com.enrollment.system.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileController {
    
    @FXML
    private Label lblFullName;
    
    @FXML
    private Label lblUsername;
    
    @FXML
    private Label lblEmail;
    
    @FXML
    private Label lblRole;
    
    @FXML
    private Label lblStatus;
    
    @FXML
    private PasswordField txtCurrentPassword;
    
    @FXML
    private PasswordField txtNewPassword;
    
    @FXML
    private PasswordField txtConfirmPassword;
    
    @FXML
    private Label lblPasswordError;
    
    @FXML
    private Label lblPasswordSuccess;
    
    @FXML
    private Button btnChangePassword;
    
    @FXML
    private Button btnCancelPassword;
    
    @Autowired
    private AuthService authService;
    
    private UserDto currentUser;
    private String sessionToken;
    
    @FXML
    public void initialize() {
        hideMessages();
    }
    
    public void setUserSession(UserDto user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
        loadUserProfile();
    }
    
    private void loadUserProfile() {
        if (currentUser != null) {
            Platform.runLater(() -> {
                lblFullName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "N/A");
                lblUsername.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "N/A");
                lblEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");
                lblRole.setText(currentUser.getRoleDisplayName() != null ? currentUser.getRoleDisplayName() : "N/A");
                lblStatus.setText(currentUser.getIsActive() != null && currentUser.getIsActive() ? "✓ Active" : "✗ Inactive");
                
                if (currentUser.getIsActive() != null && currentUser.getIsActive()) {
                    lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else {
                    lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            });
        }
    }
    
    @FXML
    private void handleChangePassword() {
        hideMessages();
        
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        
        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required");
            return;
        }
        
        if (newPassword.length() < 8) {
            showError("New password must be at least 8 characters long");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("New password and confirm password do not match");
            return;
        }
        
        if (currentPassword.equals(newPassword)) {
            showError("New password must be different from current password");
            return;
        }
        
        // Disable button during processing
        btnChangePassword.setDisable(true);
        
        // Perform password change in background thread
        new Thread(() -> {
            try {
                boolean success = authService.changePassword(
                    currentUser.getUsername(),
                    currentPassword,
                    newPassword,
                    sessionToken
                );
                
                Platform.runLater(() -> {
                    btnChangePassword.setDisable(false);
                    
                    if (success) {
                        showSuccess("Password changed successfully!");
                        clearPasswordFields();
                    } else {
                        showError("Failed to change password. Please check your current password.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnChangePassword.setDisable(false);
                    showError("Error changing password: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleCancelPassword() {
        clearPasswordFields();
        hideMessages();
    }
    
    private void clearPasswordFields() {
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
    }
    
    private void showError(String message) {
        lblPasswordError.setText(message);
        lblPasswordError.setVisible(true);
        lblPasswordError.setManaged(true);
        lblPasswordSuccess.setVisible(false);
        lblPasswordSuccess.setManaged(false);
    }
    
    private void showSuccess(String message) {
        lblPasswordSuccess.setText(message);
        lblPasswordSuccess.setVisible(true);
        lblPasswordSuccess.setManaged(true);
        lblPasswordError.setVisible(false);
        lblPasswordError.setManaged(false);
    }
    
    private void hideMessages() {
        lblPasswordError.setVisible(false);
        lblPasswordError.setManaged(false);
        lblPasswordSuccess.setVisible(false);
        lblPasswordSuccess.setManaged(false);
    }
}

