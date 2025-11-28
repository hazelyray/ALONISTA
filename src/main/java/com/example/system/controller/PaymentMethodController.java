package com.example.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

@Component
public class PaymentMethodController {

    @Autowired
    private ApplicationContext springContext;

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/TuitionBalance.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) getCurrentNode().getScene().getWindow();
            // ⭐ FIX: Preserve window state
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            
            stage.setTitle("Tuition Balance");
            // ⭐ Restore the previous window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            
            System.out.println("Navigated back to Tuition Balance (preserved window state)");
        } catch (Exception e) {
            System.err.println("Could not load tuition balance screen");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGCash() {
        loadPaymentScreen("GCashPayment.fxml", "GCash Payment");
    }

    @FXML
    public void handleGoTyme() {
        loadPaymentScreen("GoTymePayment.fxml", "GoTyme Payment");
    }

    private void loadPaymentScreen(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/" + fxmlFile));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) getCurrentNode().getScene().getWindow();
            // ⭐ FIX: Preserve window state
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            
            stage.setTitle(title);
            // ⭐ Restore the previous window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            
            System.out.println("Navigated to " + title + " (preserved window state)");
        } catch (Exception e) {
            System.err.println("Could not load " + fxmlFile);
            e.printStackTrace();
            // Fallback to placeholder if specific payment screen doesn't exist
            createPlaceholderPaymentScreen(title);
        }
    }

    // Add this helper method to get a node from the current scene
    private javafx.scene.Node getCurrentNode() {
        // Get the first window that's showing
        Window window = javafx.stage.Stage.getWindows().stream()
            .filter(Window::isShowing)
            .findFirst()
            .orElse(null);
        
        if (window instanceof Stage) {
            Scene scene = ((Stage) window).getScene();
            if (scene != null && scene.getRoot() != null) {
                return scene.getRoot();
            }
        }
        
        // Fallback - this should rarely happen
        return new Label(); // dummy node
    }

    // Also update the createPlaceholderPaymentScreen method:
    private void createPlaceholderPaymentScreen(String paymentMethod) {
        try {
            // Create a simple placeholder screen
            VBox root = new VBox(20);
            root.setPadding(new javafx.geometry.Insets(40));
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setStyle("-fx-background-color: #ecf0f1;");
            
            Label titleLabel = new Label(paymentMethod + " - Coming Soon");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            Label messageLabel = new Label("The " + paymentMethod + " integration is currently under development.\n\nYou will be able to complete your payment securely through this method soon.");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-text-alignment: center;");
            messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            Button backButton = new Button("← Back to Payment Methods");
            backButton.setStyle("-fx-background-color: #1e88e5; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20; -fx-cursor: hand;");
            backButton.setOnAction(e -> handleBack());
            
            root.getChildren().addAll(titleLabel, messageLabel, backButton);
            
            Stage stage = (Stage) getCurrentNode().getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            
            stage.setTitle(paymentMethod);
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}