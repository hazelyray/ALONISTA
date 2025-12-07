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
import com.enrollment.system.service.SubjectService;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
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
    private Button btnReports;
    
    @FXML
    private Button btnAccountSettings;
    
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
    
    @Autowired(required = false)
    private SubjectService subjectService;
    
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
        
        if (btnReports != null) {
            final String[] originalStyle = {defaultStyle};
            btnReports.setOnMouseEntered(e -> {
                originalStyle[0] = btnReports.getStyle();
                btnReports.setStyle(hoverStyle);
            });
            btnReports.setOnMouseExited(e -> btnReports.setStyle(originalStyle[0]));
        }
        
        if (btnAccountSettings != null) {
            final String[] originalStyle = {defaultStyle};
            btnAccountSettings.setOnMouseEntered(e -> {
                originalStyle[0] = btnAccountSettings.getStyle();
                btnAccountSettings.setStyle(hoverStyle);
            });
            btnAccountSettings.setOnMouseExited(e -> btnAccountSettings.setStyle(originalStyle[0]));
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
    
    @FXML
    private void showReports() {
        resetButtonStyles();
        if (btnReports != null) {
            btnReports.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0);");
        }
        loadReportsPage();
    }
    
    @FXML
    private void showAccountSettings() {
        resetButtonStyles();
        if (btnAccountSettings != null) {
            btnAccountSettings.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 0);");
        }
        loadAccountSettingsPage();
    }
    
    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-alignment: center-left; -fx-background-radius: 8; -fx-cursor: hand; -fx-pref-width: 250; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 2, 2);";
        if (btnDashboard != null) btnDashboard.setStyle(defaultStyle);
        if (btnMySubjects != null) btnMySubjects.setStyle(defaultStyle);
        if (btnMyStudents != null) btnMyStudents.setStyle(defaultStyle);
        if (btnReports != null) btnReports.setStyle(defaultStyle);
        if (btnAccountSettings != null) btnAccountSettings.setStyle(defaultStyle);
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
                // Get actual assignments from TeacherAssignment table (NEW SYSTEM)
                java.util.Map<Long, List<Section>> subjectSectionMap = teacherService.getSubjectSectionMap(currentUser.getId());
                
                // Extract unique subjects from assignments
                java.util.Set<Long> subjectIds = subjectSectionMap.keySet();
                List<Subject> teacherSubjects = new java.util.ArrayList<>();
                if (subjectService != null && !subjectIds.isEmpty()) {
                    for (Long subjectId : subjectIds) {
                        try {
                            Subject subject = subjectService.getSubjectRepository().findById(subjectId).orElse(null);
                            if (subject != null) {
                                teacherSubjects.add(subject);
                            }
                        } catch (Exception e) {
                            // Skip if subject not found
                        }
                    }
                }
                
                // Extract unique sections from assignments (use IDs to avoid HashSet issues)
                java.util.Set<Long> uniqueSectionIds = new java.util.HashSet<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null) {
                            uniqueSectionIds.add(section.getId());
                        }
                    }
                }
                // Convert back to List<Section> using the sections we already have
                List<Section> teacherSections = new java.util.ArrayList<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null && 
                            uniqueSectionIds.contains(section.getId()) &&
                            !teacherSections.stream().anyMatch(s -> s.getId().equals(section.getId()))) {
                            teacherSections.add(section);
                        }
                    }
                }
                
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
        
        // Load data in background thread - ONLY from actual assignments
        new Thread(() -> {
            try {
                // Get actual assignments from TeacherAssignment table
                java.util.Map<Long, List<Section>> subjectSectionMap = teacherService.getSubjectSectionMap(currentUser.getId());
                
                // Extract unique subjects from assignments
                java.util.Set<Long> subjectIds = subjectSectionMap.keySet();
                List<Subject> teacherSubjects = new java.util.ArrayList<>();
                if (subjectService != null && !subjectIds.isEmpty()) {
                    for (Long subjectId : subjectIds) {
                        try {
                            Subject subject = subjectService.getSubjectRepository().findById(subjectId).orElse(null);
                            if (subject != null) {
                                teacherSubjects.add(subject);
                            }
                        } catch (Exception e) {
                            // Skip if subject not found
                        }
                    }
                }
                
                // Extract unique sections from assignments (use IDs to avoid HashSet issues)
                java.util.Set<Long> uniqueSectionIds = new java.util.HashSet<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null) {
                            uniqueSectionIds.add(section.getId());
                        }
                    }
                }
                // Convert back to List<Section> using the sections we already have
                List<Section> teacherSections = new java.util.ArrayList<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null && 
                            uniqueSectionIds.contains(section.getId()) &&
                            !teacherSections.stream().anyMatch(s -> s.getId().equals(section.getId()))) {
                            teacherSections.add(section);
                        }
                    }
                }
                
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
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 30, 0));
        
        Label pageTitle = new Label("My Assigned Subjects");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleBox.getChildren().add(pageTitle);
        dashboardContent.getChildren().add(titleBox);
        
        if (subjects.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));
            
            Label emptyIcon = new Label("📚");
            emptyIcon.setStyle("-fx-font-size: 64px;");
            
            Label noSubjectsLabel = new Label("No subjects assigned yet.");
            noSubjectsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #95a5a6;");
            
            emptyBox.getChildren().addAll(emptyIcon, noSubjectsLabel);
            dashboardContent.getChildren().add(emptyBox);
        } else {
            // Create a grid layout for subjects
            GridPane subjectsGrid = new GridPane();
            subjectsGrid.setHgap(20);
            subjectsGrid.setVgap(20);
            subjectsGrid.setPadding(new Insets(0, 0, 20, 0));
            
            int col = 0;
            int row = 0;
            int colsPerRow = 2;
            
            // Get students data for section navigation
            final List<Section> finalSections = sections;
            java.util.Map<Long, List<StudentDto>> studentsBySection = new java.util.HashMap<>();
            if (studentService != null && currentUser != null) {
                try {
                    List<StudentDto> allStudents = studentService.getAllStudents();
                    studentsBySection = allStudents.stream()
                        .filter(s -> s.getSectionId() != null && "Enrolled".equals(s.getEnrollmentStatus()))
                        .collect(Collectors.groupingBy(StudentDto::getSectionId));
                } catch (Exception e) {
                    // Ignore - will show empty sections
                }
            }
            
            final java.util.Map<Long, List<StudentDto>> finalStudentsBySection = studentsBySection;
            
            for (Subject subject : subjects) {
                VBox subjectCard = createSubjectCard(subject, finalSections, finalStudentsBySection);
                subjectsGrid.add(subjectCard, col, row);
                
                col++;
                if (col >= colsPerRow) {
                    col = 0;
                    row++;
                }
            }
            
            dashboardContent.getChildren().add(subjectsGrid);
        }
    }
    
    private VBox createSubjectCard(Subject subject, List<Section> sections, java.util.Map<Long, List<StudentDto>> studentsBySection) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20, 20, 20, 20));
        card.setPrefWidth(350);
        card.setPrefHeight(180);
        
        // Simple, clean background - no gradient, just a subtle color
        String[] gradientColors = getGradientForGrade(subject.getGradeLevel());
        String primaryColor = gradientColors[0];
        
        card.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
            "-fx-border-color: " + primaryColor + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 12;"
        );
        
        // Subject Name - simple and clean
        Label subjectName = new Label(subject.getName());
        subjectName.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        subjectName.setWrapText(true);
        
        // Grade Level - simple badge
        Label gradeLabel = new Label("Grade " + (subject.getGradeLevel() != null ? subject.getGradeLevel() : "N/A"));
        gradeLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: " + primaryColor + "; " +
            "-fx-font-weight: 600; " +
            "-fx-background-color: " + primaryColor + "15; " +
            "-fx-padding: 4 10; " +
            "-fx-background-radius: 8;"
        );
        
        // Sections - compact display
        VBox sectionsBox = new VBox(8);
        Label sectionsTitle = new Label("Sections:");
        sectionsTitle.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #7f8c8d; " +
            "-fx-font-weight: 600;"
        );
        sectionsBox.getChildren().add(sectionsTitle);
        
        // Get sections that are actually assigned to this specific subject
        // Only show sections that are explicitly assigned - no fallback
        List<Section> matchingSections = new java.util.ArrayList<>();
        if (teacherService != null && currentUser != null) {
            try {
                matchingSections = teacherService.getSectionsForSubject(currentUser.getId(), subject.getId());
            } catch (Exception e) {
                // If there's an error, show empty list - don't show unassigned sections
                matchingSections = new java.util.ArrayList<>();
            }
        }
        
        if (matchingSections.isEmpty()) {
            Label noSectionsLabel = new Label("No sections assigned");
            noSectionsLabel.setStyle(
                "-fx-font-size: 11px; " +
                "-fx-text-fill: #95a5a6; " +
                "-fx-font-style: italic;"
            );
            sectionsBox.getChildren().add(noSectionsLabel);
        } else {
            FlowPane sectionsFlow = new FlowPane();
            sectionsFlow.setHgap(6);
            sectionsFlow.setVgap(6);
            
            final String finalPrimaryColor = primaryColor;
            for (Section section : matchingSections) {
                List<StudentDto> sectionStudents = studentsBySection.getOrDefault(section.getId(), new java.util.ArrayList<>());
                int studentCount = sectionStudents.size();
                
                Label sectionBadge = new Label(section.getName() + (studentCount > 0 ? " (" + studentCount + ")" : ""));
                sectionBadge.setStyle(
                    "-fx-background-color: " + finalPrimaryColor + "20; " +
                    "-fx-text-fill: " + finalPrimaryColor + "; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 4 10; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;"
                );
                
                // Simple hover effect
                sectionBadge.setOnMouseEntered(e -> {
                    sectionBadge.setStyle(
                        "-fx-background-color: " + finalPrimaryColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 4 10; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
                    );
                });
                
                sectionBadge.setOnMouseExited(e -> {
                    sectionBadge.setStyle(
                        "-fx-background-color: " + finalPrimaryColor + "20; " +
                        "-fx-text-fill: " + finalPrimaryColor + "; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 4 10; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
                    );
                });
                
                // Make clickable - navigate to students page and show this section
                final Section finalSection = section;
                final List<StudentDto> finalSectionStudents = sectionStudents;
                sectionBadge.setOnMouseClicked(e -> {
                    // Navigate to My Students page first
                    if (btnMyStudents != null) {
                        showMyStudents();
                    }
                    // After a short delay, show the specific section
                    new Thread(() -> {
                        try {
                            Thread.sleep(500); // Wait for page to load
                            Platform.runLater(() -> {
                                if (dashboardContent != null) {
                                    showSectionStudents(finalSection, finalSectionStudents);
                                }
                            });
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
                
                sectionsFlow.getChildren().add(sectionBadge);
            }
            sectionsBox.getChildren().add(sectionsFlow);
        }
        
        card.getChildren().addAll(subjectName, gradeLabel, sectionsBox);
        
        // Simple hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3); " +
                "-fx-border-color: " + primaryColor + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: default;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                "-fx-border-color: " + primaryColor + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: default;"
            );
        });
        
        return card;
    }
    
    private String[] getGradientForGrade(Integer gradeLevel) {
        // Professional gradient colors - clean, modern, and elegant
        // Grade 11: Professional blue gradient
        // Grade 12: Professional indigo gradient
        if (gradeLevel == null) {
            return new String[]{"#5c7cfa", "#4c6ef5", "#364fc7"}; // Default: Professional blue
        }
        
        switch (gradeLevel) {
            case 11:
                // Professional blue gradient - clean and modern
                return new String[]{"#5c7cfa", "#4c6ef5", "#364fc7"};
            case 12:
                // Professional indigo gradient - elegant and refined
                return new String[]{"#845ef7", "#7048e8", "#5f3dc4"};
            default:
                return new String[]{"#5c7cfa", "#4c6ef5", "#364fc7"}; // Default: Professional blue
        }
    }
    
    private void loadStudentsPage() {
        if (dashboardContent == null || teacherService == null || studentService == null || currentUser == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Load data in background thread - ONLY from actual assignments
        new Thread(() -> {
            try {
                // Get actual assignments from TeacherAssignment table - extract unique sections
                java.util.Map<Long, List<Section>> subjectSectionMap = teacherService.getSubjectSectionMap(currentUser.getId());
                
                // Extract unique sections from assignments (use IDs to avoid HashSet issues)
                java.util.Set<Long> uniqueSectionIds = new java.util.HashSet<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null) {
                            uniqueSectionIds.add(section.getId());
                        }
                    }
                }
                // Convert back to List<Section> using the sections we already have
                List<Section> teacherSections = new java.util.ArrayList<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null && 
                            uniqueSectionIds.contains(section.getId()) &&
                            !teacherSections.stream().anyMatch(s -> s.getId().equals(section.getId()))) {
                            teacherSections.add(section);
                        }
                    }
                }
                
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
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 30, 0));
        
        Label pageTitle = new Label("My Students");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleBox.getChildren().add(pageTitle);
        dashboardContent.getChildren().add(titleBox);
        
        // Load sections for the teacher
        if (teacherService == null || currentUser == null) {
            Label errorLabel = new Label("Unable to load sections.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-padding: 20;");
            dashboardContent.getChildren().add(errorLabel);
            return;
        }
        
        new Thread(() -> {
            try {
                // Get actual assignments from TeacherAssignment table - extract unique sections
                java.util.Map<Long, List<Section>> subjectSectionMap = teacherService.getSubjectSectionMap(currentUser.getId());
                
                // Extract unique sections from assignments (use IDs to avoid HashSet issues)
                java.util.Set<Long> uniqueSectionIds = new java.util.HashSet<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null) {
                            uniqueSectionIds.add(section.getId());
                        }
                    }
                }
                // Convert back to List<Section> using the sections we already have
                List<Section> teacherSections = new java.util.ArrayList<>();
                for (List<Section> sections : subjectSectionMap.values()) {
                    for (Section section : sections) {
                        if (section != null && section.getId() != null && 
                            uniqueSectionIds.contains(section.getId()) &&
                            !teacherSections.stream().anyMatch(s -> s.getId().equals(section.getId()))) {
                            teacherSections.add(section);
                        }
                    }
                }
                
                // Group students by section
                final List<Section> finalSections = teacherSections;
                java.util.Map<Long, List<StudentDto>> studentsBySection = students.stream()
                    .filter(s -> s.getSectionId() != null)
                    .collect(Collectors.groupingBy(StudentDto::getSectionId));
                
                Platform.runLater(() -> {
                    buildSectionsView(finalSections, studentsBySection);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error loading sections: " + e.getMessage());
                    errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-padding: 20;");
                    dashboardContent.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void buildSectionsView(List<Section> sections, java.util.Map<Long, List<StudentDto>> studentsBySection) {
        // Clear existing content except title
        if (dashboardContent.getChildren().size() > 1) {
            dashboardContent.getChildren().remove(1, dashboardContent.getChildren().size());
        }
        
        if (sections.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));
            
            Label emptyIcon = new Label("🏫");
            emptyIcon.setStyle("-fx-font-size: 64px;");
            
            Label noSectionsLabel = new Label("No sections assigned yet.");
            noSectionsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #95a5a6;");
            
            emptyBox.getChildren().addAll(emptyIcon, noSectionsLabel);
            dashboardContent.getChildren().add(emptyBox);
        } else {
            // Instructions
            Label instructionLabel = new Label("Click on a section to view its students");
            instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 20 0;");
            dashboardContent.getChildren().add(instructionLabel);
            
            // Create grid for section cards
            GridPane sectionsGrid = new GridPane();
            sectionsGrid.setHgap(20);
            sectionsGrid.setVgap(20);
            sectionsGrid.setPadding(new Insets(0, 0, 20, 0));
            
            int col = 0;
            int row = 0;
            int colsPerRow = 3;
            
            for (Section section : sections) {
                List<StudentDto> sectionStudents = studentsBySection.getOrDefault(section.getId(), new java.util.ArrayList<>());
                VBox sectionCard = createSectionCard(section, sectionStudents.size());
                sectionCard.setOnMouseClicked(e -> showSectionStudents(section, sectionStudents));
                sectionsGrid.add(sectionCard, col, row);
                
                col++;
                if (col >= colsPerRow) {
                    col = 0;
                    row++;
                }
            }
            
            dashboardContent.getChildren().add(sectionsGrid);
        }
    }
    
    private VBox createSectionCard(Section section, int studentCount) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 20, 20, 20));
        card.setPrefWidth(280);
        card.setPrefHeight(160);
        
        // Color scheme based on strand - simpler, cleaner
        String[] colors = getColorSchemeForStrand(section.getStrand());
        String primaryColor = colors[0];
        
        // Simple white background with colored border
        card.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
            "-fx-border-color: " + primaryColor + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 12;"
        );
        
        // Section Name
        Label sectionName = new Label(section.getName());
        sectionName.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        
        // Strand and Grade
        Label strandLabel = new Label(section.getStrand() + " • Grade " + (section.getGradeLevel() != null ? section.getGradeLevel() : "N/A"));
        strandLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #7f8c8d; " +
            "-fx-font-weight: 500;"
        );
        
        // Student count
        Label studentCountLabel = new Label(studentCount + " student" + (studentCount != 1 ? "s" : ""));
        studentCountLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-text-fill: " + primaryColor + "; " +
            "-fx-padding: 6 0 0 0;"
        );
        
        card.getChildren().addAll(sectionName, strandLabel, studentCountLabel);
        
        // Simple hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + primaryColor + "08; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3); " +
                "-fx-border-color: " + primaryColor + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: hand;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                "-fx-border-color: " + primaryColor + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: default;"
            );
        });
        
        return card;
    }
    
    private String[] getColorSchemeForStrand(String strand) {
        if (strand == null) {
            return new String[]{"#5c7cfa", "#4c6ef5", "#364fc7"}; // Professional blue
        }
        
        switch (strand.toUpperCase()) {
            case "ABM":
                return new String[]{"#ff6b6b", "#ee5a6f", "#ff8787"}; // Professional coral
            case "HUMSS":
                return new String[]{"#9775fa", "#845ef7", "#b197fc"}; // Professional purple
            case "STEM":
                return new String[]{"#4dabf7", "#339af0", "#74c0fc"}; // Professional blue
            case "GAS":
                return new String[]{"#51cf66", "#40c057", "#69db7c"}; // Professional green
            case "TVL":
                return new String[]{"#ff922b", "#fd7e14", "#ffa94d"}; // Professional orange
            default:
                return new String[]{"#5c7cfa", "#4c6ef5", "#364fc7"}; // Default Professional blue
        }
    }
    
    private void showSectionStudents(Section section, List<StudentDto> students) {
        dashboardContent.getChildren().clear();
        
        // Back button and title
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 25, 0));
        
        Button backButton = new Button("← Back to Sections");
        backButton.setStyle(
            "-fx-background-color: #95a5a6; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> loadStudentsPage());
        
        Label pageTitle = new Label(section.getName() + " - Students");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        headerBox.getChildren().addAll(backButton, pageTitle);
        dashboardContent.getChildren().add(headerBox);
        
        if (students.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));
            
            Label emptyIcon = new Label("👥");
            emptyIcon.setStyle("-fx-font-size: 64px;");
            
            Label noStudentsLabel = new Label("No enrolled students in this section.");
            noStudentsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #95a5a6;");
            
            emptyBox.getChildren().addAll(emptyIcon, noStudentsLabel);
            dashboardContent.getChildren().add(emptyBox);
        } else {
            TableView<StudentDto> studentsTable = createStudentsTable(students);
            studentsTable.setPrefHeight(600);
            dashboardContent.getChildren().add(studentsTable);
        }
    }
    
    private void loadReportsPage() {
        if (dashboardContent == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Page Title
        Label pageTitle = new Label("Reports");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 30 0;");
        dashboardContent.getChildren().add(pageTitle);
        
        // Empty state
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60, 20, 60, 20));
        
        Label emptyIcon = new Label("📊");
        emptyIcon.setStyle("-fx-font-size: 64px;");
        
        Label emptyLabel = new Label("Reports feature coming soon.");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #95a5a6;");
        
        emptyBox.getChildren().addAll(emptyIcon, emptyLabel);
        dashboardContent.getChildren().add(emptyBox);
    }
    
    private void loadAccountSettingsPage() {
        if (dashboardContent == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Page Title
        Label pageTitle = new Label("Account Settings");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 30 0;");
        dashboardContent.getChildren().add(pageTitle);
        
        // Empty state
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60, 20, 60, 20));
        
        Label emptyIcon = new Label("⚙️");
        emptyIcon.setStyle("-fx-font-size: 64px;");
        
        Label emptyLabel = new Label("Account settings feature coming soon.\nYou will be able to update your name, password, and profile picture.");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #95a5a6; -fx-text-alignment: center;");
        emptyLabel.setWrapText(true);
        
        emptyBox.getChildren().addAll(emptyIcon, emptyLabel);
        dashboardContent.getChildren().add(emptyBox);
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

