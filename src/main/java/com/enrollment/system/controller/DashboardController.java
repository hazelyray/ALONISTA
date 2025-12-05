package com.enrollment.system.controller;

import com.enrollment.system.dto.UserDto;
import com.enrollment.system.controller.ViewStudentsController;
import com.enrollment.system.controller.ArchiveStudentsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    private Button btnStudentManagement;
    
    @FXML
    private Button btnAddStudent;
    
    @FXML
    private Button btnViewStudents;
    
    @FXML
    private VBox studentManagementSubmenu;
    
    @FXML
    private Button btnReports;
    
    @FXML
    private Button btnByGradeLevel;
    
    @FXML
    private Button btnByStrand;
    
    @FXML
    private Button btnEnrollmentSummary;
    
    @FXML
    private Button btnArchiveStudents;
    
    @FXML
    private VBox reportsSubmenu;
    
    @FXML
    private Button btnAdminSettings;
    
    @FXML
    private Button btnProfile;
    
    @FXML
    private Button btnChangePassword;
    
    @FXML
    private Button btnSystemSettings;
    
    @FXML
    private VBox adminSettingsSubmenu;
    
    @FXML
    private VBox dashboardContent;
    
    @FXML
    private VBox adminSection;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private UserDto currentUser;
    private String sessionToken;
    private boolean studentManagementExpanded = false;
    private boolean reportsExpanded = false;
    private boolean adminSettingsExpanded = false;
    
    @FXML
    public void initialize() {
        if (btnDashboard != null) {
            btnDashboard.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 230;");
        }
    }
    
    public void setUserSession(UserDto user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
        
        if (userNameLabel != null && user != null) {
            userNameLabel.setText("Welcome, " + user.getFullName());
        }
        if (userRoleLabel != null && user != null) {
            userRoleLabel.setText("(" + user.getRoleDisplayName() + ")");
        }
        
        if (adminSection != null && user != null && !"ADMIN".equals(user.getRole())) {
            adminSection.setVisible(false);
            adminSection.setManaged(false);
        }
    }
    
    @FXML
    private void showDashboard() {
        resetMainButtonStyles();
        if (btnDashboard != null) {
            btnDashboard.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 230;");
        }
        // Blank for now
    }
    
    @FXML
    private void toggleStudentManagement() {
        studentManagementExpanded = !studentManagementExpanded;
        if (studentManagementSubmenu != null) {
            studentManagementSubmenu.setVisible(studentManagementExpanded);
            studentManagementSubmenu.setManaged(studentManagementExpanded);
        }
        if (btnStudentManagement != null) {
            btnStudentManagement.setText(studentManagementExpanded ? "Student Management ▲" : "Student Management ▼");
        }
    }
    
    @FXML
    private void showAddStudent() {
        resetSubmenuButtonStyles();
        btnAddStudent.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        
        try {
            // Load Add Student FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddStudent.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent addStudentRoot = loader.load();
            
            // Create new stage for Add Student form
            Stage addStudentStage = new Stage();
            addStudentStage.setTitle("Add New Student - Seguinon SHS Enrollment System");
            addStudentStage.setScene(new Scene(addStudentRoot));
            addStudentStage.setResizable(true);
            addStudentStage.setWidth(1200);
            addStudentStage.setHeight(750);
            addStudentStage.setResizable(false);
            
            // Set owner to dashboard stage
            Stage dashboardStage = (Stage) btnAddStudent.getScene().getWindow();
            addStudentStage.initOwner(dashboardStage);
            
            // Show Add Student form
            addStudentStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Add Student form");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showViewStudents() {
        resetSubmenuButtonStyles();
        btnViewStudents.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        
        try {
            // Load View Students FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ViewStudents.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent viewStudentsRoot = loader.load();
            
            // Get the controller to refresh data
            ViewStudentsController controller = loader.getController();
            
            // Create new stage for View Students
            Stage viewStudentsStage = new Stage();
            viewStudentsStage.setTitle("View Students - Seguinon SHS Enrollment System");
            viewStudentsStage.setScene(new Scene(viewStudentsRoot));
            viewStudentsStage.setWidth(1400);
            viewStudentsStage.setHeight(800);
            viewStudentsStage.setResizable(true);
            
            // Set owner to dashboard stage
            Stage dashboardStage = (Stage) btnViewStudents.getScene().getWindow();
            viewStudentsStage.initOwner(dashboardStage);
            
            // Refresh data when window is shown
            viewStudentsStage.setOnShown(e -> {
                if (controller != null) {
                    controller.refreshData();
                }
            });
            
            // Show View Students window
            viewStudentsStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load View Students page");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void toggleReports() {
        reportsExpanded = !reportsExpanded;
        if (reportsSubmenu != null) {
            reportsSubmenu.setVisible(reportsExpanded);
            reportsSubmenu.setManaged(reportsExpanded);
        }
        if (btnReports != null) {
            btnReports.setText(reportsExpanded ? "Reports ▲" : "Reports ▼");
        }
    }
    
    @FXML
    private void showByGradeLevel() {
        resetSubmenuButtonStyles();
        btnByGradeLevel.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        // Blank for now
    }
    
    @FXML
    private void showByStrand() {
        resetSubmenuButtonStyles();
        btnByStrand.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        // Blank for now
    }
    
    @FXML
    private void showEnrollmentSummary() {
        resetSubmenuButtonStyles();
        btnEnrollmentSummary.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        // Blank for now
    }
    
    @FXML
    private void showArchiveStudents() {
        resetSubmenuButtonStyles();
        btnArchiveStudents.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        
        try {
            // Load Archive Students FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ArchiveStudents.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent archiveStudentsRoot = loader.load();
            
            // Create new stage for Archive Students
            Stage archiveStudentsStage = new Stage();
            archiveStudentsStage.setTitle("Archive Students - Seguinon SHS Enrollment System");
            archiveStudentsStage.setScene(new Scene(archiveStudentsRoot));
            archiveStudentsStage.setWidth(1400);
            archiveStudentsStage.setHeight(800);
            archiveStudentsStage.setResizable(true);
            
            // Set owner to dashboard stage
            Stage dashboardStage = (Stage) btnArchiveStudents.getScene().getWindow();
            archiveStudentsStage.initOwner(dashboardStage);
            
            // Refresh data when window is shown
            archiveStudentsStage.setOnShown(e -> {
                ArchiveStudentsController controller = loader.getController();
                if (controller != null) {
                    controller.refreshData();
                }
            });
            
            // Show Archive Students window
            archiveStudentsStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Archive Students page");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void toggleAdminSettings() {
        adminSettingsExpanded = !adminSettingsExpanded;
        if (adminSettingsSubmenu != null) {
            adminSettingsSubmenu.setVisible(adminSettingsExpanded);
            adminSettingsSubmenu.setManaged(adminSettingsExpanded);
        }
        if (btnAdminSettings != null) {
            btnAdminSettings.setText(adminSettingsExpanded ? "Admin Settings ▲" : "Admin Settings ▼");
        }
    }
    
    @FXML
    private void showProfile() {
        resetSubmenuButtonStyles();
        btnProfile.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        // Blank for now
    }
    
    @FXML
    private void showChangePassword() {
        resetSubmenuButtonStyles();
        btnChangePassword.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        // Blank for now
    }
    
    @FXML
    private void showSystemSettings() {
        resetSubmenuButtonStyles();
        btnSystemSettings.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;");
        // Blank for now
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
            }
        });
    }
    
    private void resetMainButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 230;";
        if (btnDashboard != null) {
            btnDashboard.setStyle(defaultStyle);
        }
        if (btnStudentManagement != null) {
            btnStudentManagement.setStyle(defaultStyle);
        }
        if (btnReports != null) {
            btnReports.setStyle(defaultStyle);
        }
        if (btnAdminSettings != null) {
            btnAdminSettings.setStyle(defaultStyle);
        }
    }
    
    private void resetSubmenuButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 13px; -fx-padding: 10 20; -fx-alignment: center-left; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 210;";
        if (btnAddStudent != null) btnAddStudent.setStyle(defaultStyle);
        if (btnViewStudents != null) btnViewStudents.setStyle(defaultStyle);
        if (btnByGradeLevel != null) btnByGradeLevel.setStyle(defaultStyle);
        if (btnByStrand != null) btnByStrand.setStyle(defaultStyle);
        if (btnEnrollmentSummary != null) btnEnrollmentSummary.setStyle(defaultStyle);
        if (btnProfile != null) btnProfile.setStyle(defaultStyle);
        if (btnChangePassword != null) btnChangePassword.setStyle(defaultStyle);
        if (btnSystemSettings != null) btnSystemSettings.setStyle(defaultStyle);
    }
}
