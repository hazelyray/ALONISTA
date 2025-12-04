package com.enrollment.system.controller;

import com.enrollment.system.dto.UserDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class DashboardController {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label userRoleLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button btnDashboard;
    
    @FXML
    private Button btnAddStudent;
    
    @FXML
    private Button btnSearchStudent;
    
    @FXML
    private Button btnViewStudents;
    
    @FXML
    private Button btnEnrollStudent;
    
    @FXML
    private Button btnManageEnrollment;
    
    @FXML
    private Button btnManageSections;
    
    @FXML
    private Button btnManageStrands;
    
    @FXML
    private Button btnReports;
    
    @FXML
    private Button btnManageUsers;
    
    @FXML
    private Label totalStudentsLabel;
    
    @FXML
    private Label enrolledStudentsLabel;
    
    @FXML
    private Label totalSectionsLabel;
    
    @FXML
    private Label pendingEnrollmentsLabel;
    
    @FXML
    private Label activityLabel;
    
    @FXML
    private VBox dashboardContent;
    
    @FXML
    private VBox adminSection;
    
    private UserDto currentUser;
    private String sessionToken;
    
    @FXML
    public void initialize() {
        resetButtonStyles();
        btnDashboard.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 230;");
    }
    
    public void setUserSession(UserDto user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
        
        userNameLabel.setText("Welcome, " + user.getFullName());
        userRoleLabel.setText("(" + user.getRoleDisplayName() + ")");
        
        if (!"ADMIN".equals(user.getRole())) {
            adminSection.setVisible(false);
            adminSection.setManaged(false);
        }
        
        loadDashboardStats();
    }
    
    private void loadDashboardStats() {
        Platform.runLater(() -> {
            totalStudentsLabel.setText("0");
            enrolledStudentsLabel.setText("0");
            totalSectionsLabel.setText("0");
            pendingEnrollmentsLabel.setText("0");
            activityLabel.setText("System started successfully. Ready to enroll students!");
        });
    }
    
    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        showAlert("Dashboard", "You are on the Dashboard home page.");
    }
    
    @FXML
    private void showAddStudent() {
        setActiveButton(btnAddStudent);
        showAlert("Add Student", "Add New Student form will be loaded here.\n\nFeatures:\n- Student Information\n- Guardian Details\n- Previous School\n- Contact Information");
    }
    
    @FXML
    private void showSearchStudent() {
        setActiveButton(btnSearchStudent);
        showAlert("Search Students", "Search Students interface will be loaded here.\n\nSearch by:\n- Name\n- LRN\n- Grade Level\n- Section\n- Strand");
    }
    
    @FXML
    private void showViewStudents() {
        setActiveButton(btnViewStudents);
        showAlert("View Students", "Student List will be displayed here.\n\nFeatures:\n- View all students\n- Edit student info\n- Delete student\n- View student profile");
    }
    
    @FXML
    private void showEnrollStudent() {
        setActiveButton(btnEnrollStudent);
        showAlert("Enroll Student", "Enrollment Form will be loaded here.\n\nSteps:\n1. Select Student\n2. Choose School Year\n3. Select Grade Level (11/12)\n4. Choose Strand\n5. Assign Section\n6. Confirm Enrollment");
    }
    
    @FXML
    private void showManageEnrollment() {
        setActiveButton(btnManageEnrollment);
        showAlert("Manage Enrollments", "Enrollment Management will be loaded here.\n\nFeatures:\n- View all enrollments\n- Update enrollment status\n- Transfer to another section\n- Print enrollment slip");
    }
    
    @FXML
    private void showManageSections() {
        setActiveButton(btnManageSections);
        showAlert("Manage Sections", "Section Management will be loaded here.\n\nFeatures:\n- Add new section\n- Edit section details\n- Assign teacher adviser\n- Set capacity\n- View students in section");
    }
    
    @FXML
    private void showManageStrands() {
        setActiveButton(btnManageStrands);
        showAlert("Manage Strands", "Strand Management will be loaded here.\n\nSHS Strands:\n- ABM (Accountancy, Business, Management)\n- HUMSS (Humanities & Social Sciences)\n- STEM (Science, Technology, Engineering, Math)\n- GAS (General Academic Strand)\n- TVL (Technical-Vocational-Livelihood)");
    }
    
    @FXML
    private void showReports() {
        setActiveButton(btnReports);
        showAlert("Generate Reports", "Report Generation will be loaded here.\n\nReports:\n- Enrollment Summary\n- Students by Strand\n- Students by Section\n- Enrollment Statistics\n- Student List");
    }
    
    @FXML
    private void showManageUsers() {
        setActiveButton(btnManageUsers);
        if ("ADMIN".equals(currentUser.getRole())) {
            showAlert("Manage Users", "User Management will be loaded here.\n\nFeatures:\n- Add new users\n- Edit user roles\n- Deactivate users\n- Reset passwords\n\nRoles:\n- Admin\n- Registrar\n- Staff");
        } else {
            showAlert("Access Denied", "Only administrators can access User Management.");
        }
    }
    
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be returned to the login screen.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.close();
                showAlert("Logged Out", "You have been logged out successfully.");
            }
        });
    }
    
    private void setActiveButton(Button activeButton) {
        resetButtonStyles();
        activeButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 230;");
    }
    
    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 230;";
        
        btnDashboard.setStyle(defaultStyle);
        btnAddStudent.setStyle(defaultStyle);
        btnSearchStudent.setStyle(defaultStyle);
        btnViewStudents.setStyle(defaultStyle);
        btnEnrollStudent.setStyle(defaultStyle);
        btnManageEnrollment.setStyle(defaultStyle);
        btnManageSections.setStyle(defaultStyle);
        btnManageStrands.setStyle(defaultStyle);
        btnReports.setStyle(defaultStyle);
        if (btnManageUsers != null) {
            btnManageUsers.setStyle(defaultStyle);
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}