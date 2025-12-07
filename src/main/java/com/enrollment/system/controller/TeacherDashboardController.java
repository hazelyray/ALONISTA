package com.enrollment.system.controller;

import com.enrollment.system.dto.UserDto;
import com.enrollment.system.dto.StudentDto;
import com.enrollment.system.dto.SchoolYearDto;
import com.enrollment.system.model.Subject;
import com.enrollment.system.model.Section;
import com.enrollment.system.service.AuthService;
import com.enrollment.system.service.TeacherService;
import com.enrollment.system.service.SchoolYearService;
import com.enrollment.system.service.StudentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeacherDashboardController {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label userRoleLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button btnDashboard;
    
    @FXML
    private Button btnMySubjects;
    
    @FXML
    private Button btnMyStudents;
    
    @FXML
    private VBox dashboardContent;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private AuthService authService;
    
    @Autowired(required = false)
    private TeacherService teacherService;
    
    @Autowired(required = false)
    private SchoolYearService schoolYearService;
    
    @Autowired(required = false)
    private StudentService studentService;
    
    private UserDto currentUser;
    private String sessionToken;
    private Stage loginStage;
    
    @FXML
    public void initialize() {
        // Set up button styles
        try {
            if (btnDashboard != null) {
                btnDashboard.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0);");
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Set up button hover effects
        setupButtonHoverEffects();
    }
    
    private void setupButtonHoverEffects() {
        String defaultStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 2, 2);";
        String hoverStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0); -fx-translate-x: 5;";
        
        if (btnMySubjects != null) {
            final String[] originalStyle = {defaultStyle};
            btnMySubjects.setOnMouseEntered(e -> {
                originalStyle[0] = btnMySubjects.getStyle();
                btnMySubjects.setStyle(hoverStyle);
            });
            btnMySubjects.setOnMouseExited(e -> btnMySubjects.setStyle(originalStyle[0]));
        }
        
        if (btnMyStudents != null) {
            final String[] originalStyle = {defaultStyle};
            btnMyStudents.setOnMouseEntered(e -> {
                originalStyle[0] = btnMyStudents.getStyle();
                btnMyStudents.setStyle(hoverStyle);
            });
            btnMyStudents.setOnMouseExited(e -> btnMyStudents.setStyle(originalStyle[0]));
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
        
        // Set up close handler
        Platform.runLater(() -> {
            try {
                Stage dashboardStage = (Stage) (userNameLabel != null && userNameLabel.getScene() != null ? userNameLabel.getScene().getWindow() : null);
                if (dashboardStage != null) {
                    dashboardStage.setOnCloseRequest(event -> {
                        if (loginStage != null) {
                            loginStage.show();
                        } else {
                            loadLoginPage();
                        }
                    });
                }
            } catch (Exception e) {
                // Ignore
            }
        });
        
        // Load dashboard content after user session is set
        Platform.runLater(() -> {
            try {
                loadDashboardContent();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }
    
    @FXML
    private void showDashboard() {
        resetButtonStyles();
        if (btnDashboard != null) {
            btnDashboard.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0);");
        }
        loadDashboardContent();
    }
    
    @FXML
    private void showMySubjects() {
        resetButtonStyles();
        if (btnMySubjects != null) {
            btnMySubjects.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0);");
        }
        loadSubjectsPage();
    }
    
    @FXML
    private void showMyStudents() {
        resetButtonStyles();
        if (btnMyStudents != null) {
            btnMyStudents.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0);");
        }
        loadStudentsPage();
    }
    
    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 2, 2);";
        if (btnDashboard != null) btnDashboard.setStyle(defaultStyle);
        if (btnMySubjects != null) btnMySubjects.setStyle(defaultStyle);
        if (btnMyStudents != null) btnMyStudents.setStyle(defaultStyle);
    }
    
    private void loadDashboardContent() {
        if (dashboardContent == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Check if services are available
        if (teacherService == null || schoolYearService == null || studentService == null || currentUser == null) {
            Label errorLabel = new Label("Dashboard data is not available. Please ensure all services are properly configured.");
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-padding: 20;");
            dashboardContent.getChildren().add(errorLabel);
            return;
        }
        
        // Load data in background thread
        new Thread(() -> {
            try {
                // Get teacher data
                List<Subject> teacherSubjects = teacherService.getTeacherSubjects(currentUser.getId());
                List<Section> teacherSections = teacherService.getTeacherSections(currentUser.getId());
                
                // Get current school year
                String schoolYearText = "N/A";
                try {
                    SchoolYearDto currentSchoolYear = schoolYearService.getCurrentSchoolYear();
                    schoolYearText = currentSchoolYear.getYear();
                } catch (Exception e) {
                    // No current school year set - keep default "N/A"
                }
                
                // Get students from teacher's assigned sections
                List<StudentDto> allStudents = studentService.getAllStudents();
                final List<Section> finalTeacherSections = teacherSections;
                final String finalSchoolYearText = schoolYearText;
                List<StudentDto> teacherStudents = allStudents.stream()
                    .filter(student -> {
                        if (student.getSectionId() == null) {
                            return false;
                        }
                        // Check if student's section is in teacher's assigned sections
                        return finalTeacherSections.stream()
                            .anyMatch(section -> section.getId().equals(student.getSectionId()));
                    })
                    .filter(student -> "Enrolled".equals(student.getEnrollmentStatus()))
                    .collect(Collectors.toList());
                
                // Update UI on JavaFX thread
                final List<Subject> finalTeacherSubjects = teacherSubjects;
                final List<StudentDto> finalTeacherStudents = teacherStudents;
                final String finalTeacherName = currentUser.getFullName();
                Platform.runLater(() -> {
                    buildDashboardSummary(finalTeacherName, finalSchoolYearText, 
                        finalTeacherSubjects, finalTeacherSections, finalTeacherStudents);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error loading dashboard data: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-padding: 20;");
                    dashboardContent.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void buildDashboardSummary(String teacherName, String schoolYear, 
                                      List<Subject> subjects, List<Section> sections, 
                                      List<StudentDto> students) {
        dashboardContent.getChildren().clear();
        
        // Welcome Header
        HBox welcomeHeader = new HBox(15);
        welcomeHeader.setAlignment(Pos.CENTER_LEFT);
        welcomeHeader.setPadding(new Insets(0, 0, 30, 0));
        
        Label welcomeLabel = new Label("Welcome, " + teacherName);
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label schoolYearLabel = new Label("School Year: " + schoolYear);
        schoolYearLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-padding: 8 0 0 0;");
        
        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(welcomeLabel, schoolYearLabel);
        welcomeHeader.getChildren().add(headerBox);
        
        dashboardContent.getChildren().add(welcomeHeader);
        
        // Summary Panel
        HBox summaryPanel = new HBox(20);
        summaryPanel.setAlignment(Pos.CENTER_LEFT);
        summaryPanel.setPadding(new Insets(0, 0, 0, 0));
        
        int totalSubjects = subjects.size();
        int totalSections = sections.size();
        int totalStudents = students.size();
        
        summaryPanel.getChildren().addAll(
            createSummaryCard("📚 Total Subjects", String.valueOf(totalSubjects), "#3498db"),
            createSummaryCard("🏫 Total Sections", String.valueOf(totalSections), "#9b59b6"),
            createSummaryCard("👥 Total Students", String.valueOf(totalStudents), "#e67e22")
        );
        
        dashboardContent.getChildren().add(summaryPanel);
    }
    
    private void loadSubjectsPage() {
        if (dashboardContent == null || teacherService == null || currentUser == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Load data in background thread
        new Thread(() -> {
            try {
                List<Subject> teacherSubjects = teacherService.getTeacherSubjects(currentUser.getId());
                List<Section> teacherSections = teacherService.getTeacherSections(currentUser.getId());
                
                Platform.runLater(() -> {
                    buildSubjectsPage(teacherSubjects, teacherSections);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error loading subjects: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-padding: 20;");
                    dashboardContent.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void buildSubjectsPage(List<Subject> subjects, List<Section> sections) {
        dashboardContent.getChildren().clear();
        
        // Page Title
        Label pageTitle = new Label("My Assigned Subjects");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 25 0;");
        dashboardContent.getChildren().add(pageTitle);
        
        if (subjects.isEmpty()) {
            Label noSubjectsLabel = new Label("No subjects assigned yet.");
            noSubjectsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
            dashboardContent.getChildren().add(noSubjectsLabel);
        } else {
            VBox subjectsList = new VBox(10);
            for (Subject subject : subjects) {
                HBox subjectRow = new HBox(20);
                subjectRow.setAlignment(Pos.CENTER_LEFT);
                subjectRow.setPadding(new Insets(15, 20, 15, 20));
                subjectRow.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"
                );
                
                Label subjectName = new Label(subject.getName());
                subjectName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-pref-width: 350;");
                
                Label gradeLabel = new Label("Grade " + (subject.getGradeLevel() != null ? subject.getGradeLevel() : "N/A"));
                gradeLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #7f8c8d; -fx-pref-width: 120;");
                
                // Find sections that match this subject's grade level
                String sectionNames = sections.stream()
                    .filter(section -> subject.getGradeLevel() != null && 
                            section.getGradeLevel() != null && 
                            section.getGradeLevel().equals(subject.getGradeLevel()))
                    .map(Section::getName)
                    .collect(Collectors.joining(", "));
                
                if (sectionNames.isEmpty()) {
                    sectionNames = "No matching sections";
                }
                
                Label sectionLabel = new Label("Sections: " + sectionNames);
                sectionLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #7f8c8d;");
                sectionLabel.setWrapText(true);
                
                subjectRow.getChildren().addAll(subjectName, gradeLabel, sectionLabel);
                subjectsList.getChildren().add(subjectRow);
            }
            dashboardContent.getChildren().add(subjectsList);
        }
    }
    
    private void loadStudentsPage() {
        if (dashboardContent == null || teacherService == null || studentService == null || currentUser == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Load data in background thread
        new Thread(() -> {
            try {
                List<Section> teacherSections = teacherService.getTeacherSections(currentUser.getId());
                List<StudentDto> allStudents = studentService.getAllStudents();
                
                final List<Section> finalTeacherSections = teacherSections;
                List<StudentDto> teacherStudents = allStudents.stream()
                    .filter(student -> {
                        if (student.getSectionId() == null) {
                            return false;
                        }
                        return finalTeacherSections.stream()
                            .anyMatch(section -> section.getId().equals(student.getSectionId()));
                    })
                    .filter(student -> "Enrolled".equals(student.getEnrollmentStatus()))
                    .collect(Collectors.toList());
                
                Platform.runLater(() -> {
                    buildStudentsPage(teacherStudents);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error loading students: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-padding: 20;");
                    dashboardContent.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void buildStudentsPage(List<StudentDto> students) {
        dashboardContent.getChildren().clear();
        
        // Page Title
        Label pageTitle = new Label("My Students");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 25 0;");
        dashboardContent.getChildren().add(pageTitle);
        
        if (students.isEmpty()) {
            Label noStudentsLabel = new Label("No enrolled students in assigned sections.");
            noStudentsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
            dashboardContent.getChildren().add(noStudentsLabel);
        } else {
            TableView<StudentDto> studentsTable = createStudentsTable(students);
            studentsTable.setPrefHeight(600);
            dashboardContent.getChildren().add(studentsTable);
        }
    }
    
    private VBox createSummaryCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setPrefHeight(120);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 36px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + color + ";"
        );
        
        Region line = new Region();
        line.setPrefHeight(3);
        line.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");
        line.setMaxWidth(50);
        
        card.getChildren().addAll(titleLabel, valueLabel, line);
        return card;
    }
    
    private TableView<StudentDto> createStudentsTable(List<StudentDto> students) {
        TableView<StudentDto> table = new TableView<>();
        table.setPrefHeight(400);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        // Name Column
        TableColumn<StudentDto, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        nameCol.setStyle("-fx-font-size: 13px;");
        
        // LRN Column
        TableColumn<StudentDto, String> lrnCol = new TableColumn<>("LRN");
        lrnCol.setCellValueFactory(new PropertyValueFactory<>("lrn"));
        lrnCol.setPrefWidth(120);
        lrnCol.setStyle("-fx-font-size: 13px;");
        
        // Grade Level Column
        TableColumn<StudentDto, Integer> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("gradeLevel"));
        gradeCol.setPrefWidth(80);
        gradeCol.setStyle("-fx-font-size: 13px;");
        
        // Strand Column
        TableColumn<StudentDto, String> strandCol = new TableColumn<>("Strand");
        strandCol.setCellValueFactory(new PropertyValueFactory<>("strand"));
        strandCol.setPrefWidth(100);
        strandCol.setStyle("-fx-font-size: 13px;");
        
        // Section Column
        TableColumn<StudentDto, String> sectionCol = new TableColumn<>("Section");
        sectionCol.setCellValueFactory(new PropertyValueFactory<>("sectionName"));
        sectionCol.setPrefWidth(150);
        sectionCol.setStyle("-fx-font-size: 13px;");
        
        // Sex Column
        TableColumn<StudentDto, String> sexCol = new TableColumn<>("Sex");
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        sexCol.setPrefWidth(80);
        sexCol.setStyle("-fx-font-size: 13px;");
        
        // Contact Number Column
        TableColumn<StudentDto, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        contactCol.setPrefWidth(120);
        contactCol.setStyle("-fx-font-size: 13px;");
        
        @SuppressWarnings("unchecked")
        TableColumn<StudentDto, ?>[] columns = new TableColumn[] {
            nameCol, lrnCol, gradeCol, strandCol, sectionCol, sexCol, contactCol
        };
        table.getColumns().addAll(columns);
        
        ObservableList<StudentDto> studentList = FXCollections.observableArrayList(students);
        table.setItems(studentList);
        
        return table;
    }
    
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be returned to the login screen.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (sessionToken != null) {
                        try {
                            authService.logout(sessionToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    Stage dashboardStage = (Stage) logoutButton.getScene().getWindow();
                    dashboardStage.close();
                    
                    if (loginStage != null) {
                        loginStage.show();
                    } else {
                        loadLoginPage();
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Logout Error");
                    errorAlert.setHeaderText("Failed to logout");
                    errorAlert.setContentText("Error: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    
    private void loadLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginRoot = loader.load();
            
            Stage newLoginStage = new Stage();
            newLoginStage.setTitle("Seguinon SASHS - Login");
            newLoginStage.setScene(new Scene(loginRoot));
            newLoginStage.setResizable(false);
            newLoginStage.centerOnScreen();
            newLoginStage.setWidth(900);
            newLoginStage.setHeight(600);
            
            this.loginStage = newLoginStage;
            newLoginStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load login page");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}

