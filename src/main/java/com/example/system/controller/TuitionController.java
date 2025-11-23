package com.example.system.controller;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.system.dtos.LoginResponse;
import com.example.system.models.StudentBalance;
import com.example.system.models.User;
import com.example.system.repositories.StudentBalanceRepository;
import com.example.system.repositories.UserRepository;
import com.example.system.session.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

@Component
public class TuitionController {

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
    private TableView paymentTable;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentBalanceRepository studentBalanceRepository;

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        LoginResponse currentUser = sessionManager.getCurrentUser();

        if (currentUser != null) {
            loadStudentData(currentUser.getUsername());
        } else {
            System.out.println("No user found in session");
        }
    }

    // Load Login.fxml
    private void loadStudentData(String username) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && user.getStudentBalance() != null) {
            StudentBalance balance = user.getStudentBalance();

            studentNameLabel.setText(user.getFullName());
            studentIdLabel.setText(balance.getStudentId());
            emailLabel.setText(user.getEmail());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            totalFeeLabel.setText(formatter.format(balance.getTotalTuitionFee()));
            amountPaidLabel.setText(formatter.format(balance.getAmountPaid()));
            remainingBalanceLabel.setText(formatter.format(balance.getRemainingBalance()));
        }
    }

    @FXML
    public void handleBack() {
        try {
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Homepage.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("PaySTI - Home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}