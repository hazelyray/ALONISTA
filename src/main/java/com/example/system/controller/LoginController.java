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
    private TextField passwordVisibleField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Button loginButton;

    private boolean isPasswordVisible = false;

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ApplicationContext springContext;

    @FXML
    public void initialize() {
        System.out.println("Login Controller initialized");

        // Sync password fields
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isPasswordVisible) passwordVisibleField.setText(newVal);
        });

        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isPasswordVisible) passwordField.setText(newVal);
        });
    }

    @FXML
    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.setText("🙈");
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            togglePasswordButton.setText("👁");
        }
    }

    @FXML
    public void handleMouseEntered() {
        loginButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12;");
    }

    @FXML
    public void handleMouseExited() {
        loginButton.setStyle("-fx-background-color: #1e88e5; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12;");
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password");
            return;
        }

        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            LoginResponse response = authService.login(loginRequest);

            sessionManager.setCurrentUser(response);

            System.out.println("Login successful for: " + response.getFullName());
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + response.getFullName() + "!");

            loadHomepage();

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred");
            e.printStackTrace();
        }
    }

    private void loadHomepage() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = stage.getScene(); // get existing scene

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Homepage.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Parent homepageRoot = loader.load();

            // ⭐ KEY FIX: Replace only the root of the existing scene
            scene.setRoot(homepageRoot);

            stage.setTitle("PaySTI - Home");

            System.out.println("Homepage loaded without resizing.");

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
