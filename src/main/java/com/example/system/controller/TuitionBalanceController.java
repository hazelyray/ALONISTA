package com.example.system.controller;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.system.dtos.LoginResponse;
import com.example.system.models.StudentBalance;
import com.example.system.models.User;
import com.example.system.repositories.UserRepository;
import com.example.system.session.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@Component
public class TuitionBalanceController implements Initializable {
    
    @FXML
    private Label studentNameLabel;
    
    @FXML
    private Label studentIdLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label totalFeeLabel;
    
    @FXML
    private Label amountPaidLabel;
    
    @FXML
    private Label remainingBalanceLabel;
    
    @FXML
    private Label semesterLabel;
    
    @FXML
    private Label schoolYearLabel;
    
    @FXML
    private Label prelimsLabel;
    
    @FXML
    private Label midtermsLabel;
    
    @FXML
    private Label preFinalsLabel;
    
    @FXML
    private Label finalsLabel;
    
    @FXML
    private Label prelimsStatusLabel;
    
    @FXML
    private Label midtermsStatusLabel;
    
    @FXML
    private Label preFinalsStatusLabel;
    
    @FXML
    private Label finalsStatusLabel;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private ApplicationContext springContext;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("TuitionBalanceController initialized");
        
        // Debug: Check FXML injection
        System.out.println("=== FXML Injection Status ===");
        System.out.println("semesterLabel: " + semesterLabel);
        System.out.println("studentNameLabel: " + studentNameLabel);
        System.out.println("prelimsLabel: " + prelimsLabel);
        System.out.println("prelimsStatusLabel: " + prelimsStatusLabel);
        System.out.println("=============================");
        
        // Load data after FXML injection is complete
        loadStudentData();
    }
    
    private void loadStudentData() {
        // ADD NULL CHECKS to prevent the error
        if (semesterLabel == null) {
            System.err.println("ERROR: semesterLabel is null - FXML injection failed!");
            return;
        }
        
        LoginResponse currentUser = sessionManager.getCurrentUser();
        
        if (currentUser != null) {
            User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            
            if (user != null) {
                // Set student info with null checks
                if (studentNameLabel != null) studentNameLabel.setText(user.getFullName());
                if (studentIdLabel != null) studentIdLabel.setText(user.getStudentId() != null ? user.getStudentId() : "N/A");
                if (emailLabel != null) emailLabel.setText(user.getEmail());
                
                // Load balance info
                if (user.getStudentBalance() != null) {
                    StudentBalance balance = user.getStudentBalance();
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                    
                    if (totalFeeLabel != null) totalFeeLabel.setText(formatter.format(balance.getTotalTuitionFee()));
                    if (amountPaidLabel != null) amountPaidLabel.setText(formatter.format(balance.getAmountPaid()));
                    if (remainingBalanceLabel != null) remainingBalanceLabel.setText(formatter.format(balance.getRemainingBalance()));
                    if (semesterLabel != null) semesterLabel.setText(balance.getSemester());
                    if (schoolYearLabel != null) schoolYearLabel.setText(balance.getSchoolYear());
                    
                    // Calculate term payments: Each term is exactly ₱9,500
                    double termAmount = 9500.00;
                    double amountPaid = balance.getAmountPaid() != null ? balance.getAmountPaid() : 0.0;
                    
                    // Calculate how many terms have been fully paid
                    int termsPaid = (int) Math.floor(amountPaid / termAmount);
                    
                    // Display each term: 0.00 if paid, 9,500 if not paid
                    if (prelimsLabel != null) prelimsLabel.setText(termsPaid >= 1 ? formatter.format(0.00) : formatter.format(termAmount));
                    if (midtermsLabel != null) midtermsLabel.setText(termsPaid >= 2 ? formatter.format(0.00) : formatter.format(termAmount));
                    if (preFinalsLabel != null) preFinalsLabel.setText(termsPaid >= 3 ? formatter.format(0.00) : formatter.format(termAmount));
                    if (finalsLabel != null) finalsLabel.setText(termsPaid >= 4 ? formatter.format(0.00) : formatter.format(termAmount));
                    
                    // Set term status labels
                    if (prelimsStatusLabel != null) prelimsStatusLabel.setText(termsPaid >= 1 ? "Paid" : "Unpaid");
                    if (midtermsStatusLabel != null) midtermsStatusLabel.setText(termsPaid >= 2 ? "Paid" : "Unpaid");
                    if (preFinalsStatusLabel != null) preFinalsStatusLabel.setText(termsPaid >= 3 ? "Paid" : "Unpaid");
                    if (finalsStatusLabel != null) finalsStatusLabel.setText(termsPaid >= 4 ? "Paid" : "Unpaid");
                    
                    System.out.println("Loaded balance for: " + user.getFullName());
                    System.out.println("Amount paid: " + formatter.format(amountPaid));
                    System.out.println("Terms paid: " + termsPaid + " out of 4");
                }
            }
        } else {
            System.err.println("No current user found in session");
        }
    }
    
    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Homepage.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            // ⭐ FIX: Preserve window state
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            
            stage.setTitle("PaySTI - Home");
            // ⭐ Restore the previous window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            
            System.out.println("Navigated back to Homepage (preserved window state)");
        } catch (Exception e) {
            System.err.println("Could not load homepage");
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleMakePayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PaymentMethod.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            // ⭐ FIX: Preserve window state
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            
            stage.setTitle("Choose Payment Method");
            // ⭐ Restore the previous window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            
            System.out.println("Navigated to Payment Method selection (preserved window state)");
        } catch (Exception e) {
            System.err.println("Could not load payment method screen");
            e.printStackTrace();
        }
    }
}