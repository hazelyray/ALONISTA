package com.example.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.system.dtos.LoginResponse;
import com.example.system.models.StudentBalance;
import com.example.system.models.User;
import com.example.system.repositories.UserRepository;
import com.example.system.session.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;

@Component
public class HomeController {

    @FXML
    private Label welcomeLabel;  // Top right: "Welcome, Student"

    @FXML
    private Label tuitionAmountLabel;  // Card showing balance amount

    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private UserRepository userRepository;

    @FXML
    public void initialize() {
        System.out.println("HomeController loaded - Dashboard page");
        loadUserData();
    }

    private void loadUserData() {
        LoginResponse currentUser = sessionManager.getCurrentUser();
        
        if (currentUser != null) {
            System.out.println("Loading data for: " + currentUser.getFullName());
            
            // Update welcome label in top bar
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            }

            // Load balance for the card
            User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            if (user != null && user.getStudentBalance() != null) {
                StudentBalance balance = user.getStudentBalance();
                
                if (tuitionAmountLabel != null) {
                    String balanceText = "₱" + String.format("%,.2f", balance.getRemainingBalance());
                    tuitionAmountLabel.setText(balanceText);
                    System.out.println("Tuition balance: " + balanceText);
                }
            } else {
                if (tuitionAmountLabel != null) {
                    tuitionAmountLabel.setText("₱0.00");
                }
            }
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        sessionManager.clearSession();
        System.out.println("User logged out");
        navigateToLogin(event);
    }

    @FXML
    public void handleTuitionBalance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/TuitionBalance.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = getStageFromEvent(event);
            if (stage != null) {
                // ⭐ FIX: Preserve window state instead of forcing maximized
                boolean wasMaximized = stage.isMaximized();
                Scene currentScene = stage.getScene();
                currentScene.setRoot(root);
                
                stage.setTitle("Tuition Balance");
                // ⭐ Restore the previous window state
                if (wasMaximized) {
                    stage.setMaximized(true);
                }
                
                System.out.println("Navigated to Tuition Balance page (preserved window state)");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Tuition Balance page: " + e.getMessage());
        }
    }

    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = getStageFromEvent(event);
            if (stage != null) {
                boolean wasMaximized = stage.isMaximized();
                Scene currentScene = stage.getScene();
                currentScene.setRoot(root);
                
                stage.setTitle("Login - PaySTI");
                if (wasMaximized) {
                    stage.setMaximized(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Login page: " + e.getMessage());
        }
    }

    private Stage getStageFromEvent(ActionEvent event) {
        if (event == null) {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    return (Stage) window;
                }
            }
            return null;
        }
        
        Object source = event.getSource();
        try {
            if (source instanceof MenuItem) {
                MenuItem item = (MenuItem) source;
                return (Stage) item.getParentPopup().getOwnerWindow();
            } else {
                return (Stage) ((javafx.scene.Node) source).getScene().getWindow();
            }
        } catch (Exception e) {
            for (Window w : Window.getWindows()) {
                if (w instanceof Stage && w.isShowing()) {
                    return (Stage) w;
                }
            }
            return null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.show();
    }
}