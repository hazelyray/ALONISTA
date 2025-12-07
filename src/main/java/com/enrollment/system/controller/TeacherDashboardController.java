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
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.FileOutputStream;

@Component
public class TeacherDashboardController {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label userRoleLabel;
    
    @FXML
    private Label dateTimeLabel;
    
    @FXML
    private StackPane profilePictureContainer;
    
    @FXML
    private ImageView profileImageView;
    
    @FXML
    private Label profileInitialsLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button btnDashboard;
    
    @FXML
    private Button btnMySubjects;
    
    @FXML
    private Button btnMyStudents;
    
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
        
        if (btnAccountSettings != null) {
            final String[] originalStyle = {defaultStyle};
            btnAccountSettings.setOnMouseEntered(e -> {
                originalStyle[0] = btnAccountSettings.getStyle();
                btnAccountSettings.setStyle(hoverStyle);
            });
            btnAccountSettings.setOnMouseExited(e -> btnAccountSettings.setStyle(originalStyle[0]));
        }
    }
    
    private Timeline dateTimeTimeline;
    
    public void setUserSession(UserDto user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
        
        // Update user info in header (without "Welcome")
        Platform.runLater(() -> {
            if (userNameLabel != null && user != null) {
                userNameLabel.setText(user.getFullName());
            }
            if (userRoleLabel != null && user != null) {
                userRoleLabel.setText(user.getRoleDisplayName());
            }
            
            // Load and display profile picture or initials
            loadProfilePicture();
            
            // Start date/time update
            startDateTimeUpdate();
        });
        
        // Set up close handler
        Platform.runLater(() -> {
            try {
                Stage dashboardStage = (Stage) (userNameLabel != null && userNameLabel.getScene() != null ? userNameLabel.getScene().getWindow() : null);
                if (dashboardStage != null) {
                    dashboardStage.setOnCloseRequest(event -> {
                        // Stop date/time timeline
                        if (dateTimeTimeline != null) {
                            dateTimeTimeline.stop();
                        }
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
    
    private void loadProfilePicture() {
        if (profilePictureContainer == null || profileImageView == null || profileInitialsLabel == null || currentUser == null) {
            return;
        }
        
        try {
            // Ensure ImageView is properly sized
            profileImageView.setFitWidth(40);
            profileImageView.setFitHeight(40);
            profileImageView.setPreserveRatio(true);
            profileImageView.setSmooth(true);
            profileImageView.setCache(true);
            
            // Update clip when layout bounds change to ensure proper centering
            profileImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                    double radius = Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0;
                    Circle clip = new Circle(
                        newBounds.getWidth() / 2.0,
                        newBounds.getHeight() / 2.0,
                        radius
                    );
                    profileImageView.setClip(clip);
                }
            });
            
            // Set initial clip
            Circle initialClip = new Circle(20, 20, 20);
            profileImageView.setClip(initialClip);
            
            // Try to load profile picture
            boolean pictureLoaded = false;
            if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
                try {
                    File imageFile = new File(currentUser.getProfilePicture());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString(), 40, 40, true, true, true);
                        profileImageView.setImage(image);
                        profileImageView.setVisible(true);
                        profileInitialsLabel.setVisible(false);
                        pictureLoaded = true;
                    }
                } catch (Exception e) {
                    // If image fails to load, show initials
                }
            }
            
            // If no picture loaded, show initials
            if (!pictureLoaded) {
                String initials = getInitials(currentUser.getFullName());
                profileInitialsLabel.setText(initials);
                profileImageView.setVisible(false);
                profileInitialsLabel.setVisible(true);
                
                // Set background color based on name hash for consistency
                String color = getColorForName(currentUser.getFullName());
                profilePictureContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 20; -fx-pref-width: 40; -fx-pref-height: 40;");
            } else {
                // If picture is loaded, ensure container has proper styling with circular background
                profilePictureContainer.setStyle("-fx-background-color: transparent; -fx-background-radius: 20; -fx-pref-width: 40; -fx-pref-height: 40;");
            }
        } catch (Exception e) {
            // Fallback to initials on any error
            if (currentUser != null) {
                String initials = getInitials(currentUser.getFullName());
                profileInitialsLabel.setText(initials);
                profileImageView.setVisible(false);
                profileInitialsLabel.setVisible(true);
                String color = getColorForName(currentUser.getFullName());
                profilePictureContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 20; -fx-pref-width: 40; -fx-pref-height: 40;");
            }
        }
    }
    
    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            // First letter of first name and first letter of last name
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        } else if (parts.length == 1) {
            // First two letters of the name
            String name = parts[0];
            if (name.length() >= 2) {
                return name.substring(0, 2).toUpperCase();
            } else {
                return name.substring(0, 1).toUpperCase();
            }
        }
        return "U";
    }
    
    private String getColorForName(String name) {
        // Generate a consistent color based on name hash
        int hash = name != null ? name.hashCode() : 0;
        String[] colors = {
            "#3498db", "#e74c3c", "#2ecc71", "#f39c12", "#9b59b6",
            "#1abc9c", "#e67e22", "#34495e", "#16a085", "#c0392b"
        };
        int index = Math.abs(hash) % colors.length;
        return colors[index];
    }
    
    private void startDateTimeUpdate() {
        if (dateTimeLabel == null) {
            return;
        }
        
        // Update immediately
        updateDateTime();
        
        // Update every minute
        dateTimeTimeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> updateDateTime()));
        dateTimeTimeline.setCycleCount(Animation.INDEFINITE);
        dateTimeTimeline.play();
    }
    
    private void updateDateTime() {
        if (dateTimeLabel == null) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a");
        String formattedDateTime = now.format(formatter);
        dateTimeLabel.setText(formattedDateTime);
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
        
        // Dashboard Title and School Year
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 30, 0));
        
        Label dashboardTitle = new Label("Dashboard");
        dashboardTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label schoolYearLabel = new Label("School Year: " + schoolYear);
        schoolYearLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-padding: 8 0 0 0;");
        
        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(dashboardTitle, schoolYearLabel);
        header.getChildren().add(headerBox);
        
        dashboardContent.getChildren().add(header);
        
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
    
    private Section currentSectionForExport = null;
    private List<StudentDto> currentSectionStudents = null;
    
    private void showSectionStudents(Section section, List<StudentDto> students) {
        dashboardContent.getChildren().clear();
        
        // Store for export functionality
        currentSectionForExport = section;
        currentSectionStudents = students;
        
        // Back button and title with export buttons
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
        
        // Export buttons (upper-right corner)
        HBox exportButtonsBox = new HBox(10);
        exportButtonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button exportPdfButton = new Button("📄 Export PDF");
        exportPdfButton.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        exportPdfButton.setOnAction(e -> exportToPDF(section, students));
        
        Button exportExcelButton = new Button("📊 Export Excel");
        exportExcelButton.setStyle(
            "-fx-background-color: #27ae60; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        exportExcelButton.setOnAction(e -> exportToExcel(section, students));
        
        exportButtonsBox.getChildren().addAll(exportPdfButton, exportExcelButton);
        
        // Header with title on left, export buttons on right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(backButton, pageTitle, spacer, exportButtonsBox);
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
            TableView<StudentDto> studentsTable = createStudentsTable(students, section);
            studentsTable.setPrefHeight(600);
            dashboardContent.getChildren().add(studentsTable);
        }
    }
    
    private void exportToPDF(Section section, List<StudentDto> students) {
        if (section == null || students == null || students.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Data");
            alert.setHeaderText("No students found in this section");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Student_List_" + section.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
        
        Stage stage = (Stage) dashboardContent.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            new Thread(() -> {
                try {
                    generatePDF(file, section, students);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("PDF exported successfully");
                        alert.setContentText("File saved to: " + file.getAbsolutePath());
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to export PDF");
                        alert.setContentText("Error: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }
    
    private void exportToExcel(Section section, List<StudentDto> students) {
        if (section == null || students == null || students.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Data");
            alert.setHeaderText("No students found in this section");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("Student_List_" + section.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx");
        
        Stage stage = (Stage) dashboardContent.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            new Thread(() -> {
                try {
                    generateExcel(file, section, students);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Excel exported successfully");
                        alert.setContentText("File saved to: " + file.getAbsolutePath());
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to export Excel");
                        alert.setContentText("Error: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }
    
    private void generatePDF(File file, Section section, List<StudentDto> students) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(842, 595));
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                PDType1Font fontBold = new PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font font = new PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);
                
                float margin = 35;
                float pageWidth = 842;
                float pageHeight = 595;
                float yPosition = pageHeight - margin;
                float lineHeight = 12;
                float headerLineHeight = 14;
                
                // Title Section
                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("STUDENT LIST BY SECTION");
                contentStream.endText();
                
                yPosition -= 25;
                
                // Section Info
                contentStream.beginText();
                contentStream.setFont(font, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Section: " + section.getName() + " | Strand: " + section.getStrand() + " | Grade Level: " + section.getGradeLevel());
                contentStream.endText();
                
                yPosition -= 18;
                
                // Date
                contentStream.beginText();
                contentStream.setFont(font, 9);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
                contentStream.endText();
                
                yPosition -= 25;
                
                // Draw separator line
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();
                
                yPosition -= 15;
                
                // Table headers - only required fields
                float[] columnWidths = {130, 80, 35, 100, 120, 150, 85}; // Name, Contact, Sex, Grade, Strand, Section, LRN
                String[] headers = {"Name", "Contact Number", "Sex", "Grade Level", "Strand", "Section", "LRN"};
                
                float totalWidth = 0;
                for (float width : columnWidths) {
                    totalWidth += width;
                }
                
                // Header row
                float headerY = yPosition;
                contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                contentStream.addRect(margin, headerY - headerLineHeight, totalWidth, headerLineHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(0, 0, 0);
                
                // Header text
                float xPosition = margin;
                contentStream.setFont(fontBold, 9);
                for (int i = 0; i < headers.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition + 2, headerY - 10);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                    xPosition += columnWidths[i];
                }
                
                yPosition -= headerLineHeight;
                
                // Draw header bottom line
                contentStream.setLineWidth(0.5f);
                contentStream.setStrokingColor(0.3f, 0.3f, 0.3f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + totalWidth, yPosition);
                contentStream.stroke();
                contentStream.setStrokingColor(0, 0, 0);
                
                yPosition -= 8;
                
                // Student data rows
                contentStream.setFont(font, 8);
                int rowNumber = 0;
                for (StudentDto student : students) {
                    if (yPosition < 60) {
                        contentStream.close();
                        page = new PDPage(new PDRectangle(842, 595));
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        fontBold = new PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD);
                        font = new PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);
                        yPosition = pageHeight - margin;
                        
                        headerY = yPosition;
                        contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                        contentStream.addRect(margin, headerY - headerLineHeight, totalWidth, headerLineHeight);
                        contentStream.fill();
                        contentStream.setNonStrokingColor(0, 0, 0);
                        
                        xPosition = margin;
                        contentStream.setFont(fontBold, 9);
                        for (int i = 0; i < headers.length; i++) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(xPosition + 2, headerY - 10);
                            contentStream.showText(headers[i]);
                            contentStream.endText();
                            xPosition += columnWidths[i];
                        }
                        
                        contentStream.setLineWidth(0.5f);
                        contentStream.setStrokingColor(0.3f, 0.3f, 0.3f);
                        contentStream.moveTo(margin, headerY - headerLineHeight);
                        contentStream.lineTo(margin + totalWidth, headerY - headerLineHeight);
                        contentStream.stroke();
                        contentStream.setStrokingColor(0, 0, 0);
                        
                        yPosition = headerY - headerLineHeight - 8;
                        contentStream.setFont(font, 8);
                    }
                    
                    if (rowNumber % 2 == 0) {
                        contentStream.setNonStrokingColor(0.98f, 0.98f, 0.98f);
                        contentStream.addRect(margin, yPosition - lineHeight, totalWidth, lineHeight);
                        contentStream.fill();
                        contentStream.setNonStrokingColor(0, 0, 0);
                    }
                    
                    xPosition = margin;
                    String[] rowData = {
                        student.getName() != null ? student.getName() : "",
                        student.getContactNumber() != null ? student.getContactNumber() : "",
                        student.getSex() != null ? student.getSex() : "",
                        student.getGradeLevel() != null ? String.valueOf(student.getGradeLevel()) : "",
                        student.getStrand() != null ? student.getStrand() : "",
                        student.getSectionName() != null ? student.getSectionName() : "",
                        student.getLrn() != null ? student.getLrn() : ""
                    };
                    
                    for (int i = 0; i < rowData.length; i++) {
                        String text = rowData[i];
                        contentStream.beginText();
                        contentStream.newLineAtOffset(xPosition + 2, yPosition - 9);
                        contentStream.showText(text);
                        contentStream.endText();
                        xPosition += columnWidths[i];
                    }
                    
                    yPosition -= lineHeight;
                    rowNumber++;
                }
                
                // Footer
                yPosition -= 15;
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();
                
                yPosition -= 12;
                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Total Students: " + students.size());
                contentStream.endText();
                
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            
            document.save(file);
        }
    }
    
    private void generateExcel(File file, Section section, List<StudentDto> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Student List");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            int rowNum = 0;
            
            // Title row
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("STUDENT LIST BY SECTION");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Section info row
            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Section: " + section.getName() + " | Strand: " + section.getStrand() + " | Grade: " + section.getGradeLevel());
            
            // Date row
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            
            rowNum++; // Empty row
            
            // Header row - only required fields
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Name", "Contact Number", "Sex", "Grade Level", "Strand", "Section", "LRN"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            for (StudentDto student : students) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                
                row.createCell(colNum++).setCellValue(student.getName() != null ? student.getName() : "");
                row.createCell(colNum++).setCellValue(student.getContactNumber() != null ? student.getContactNumber() : "");
                row.createCell(colNum++).setCellValue(student.getSex() != null ? student.getSex() : "");
                row.createCell(colNum++).setCellValue(student.getGradeLevel() != null ? student.getGradeLevel() : 0);
                row.createCell(colNum++).setCellValue(student.getStrand() != null ? student.getStrand() : "");
                row.createCell(colNum++).setCellValue(student.getSectionName() != null ? student.getSectionName() : "");
                row.createCell(colNum++).setCellValue(student.getLrn() != null ? student.getLrn() : "");
                
                // Apply data style
                for (int i = 0; i < colNum; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Footer row
            rowNum++;
            Row footerRow = sheet.createRow(rowNum);
            footerRow.createCell(0).setCellValue("Total Students: " + students.size());
            
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }
    
    private void showEditStudentModal(StudentDto student, Section section, TableView<StudentDto> table, List<StudentDto> studentsList) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit Student Information");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Name Field
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Name *");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TextField nameField = new TextField(student.getName());
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        // Contact Number Field
        VBox contactBox = new VBox(5);
        Label contactLabel = new Label("Contact Number");
        contactLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TextField contactField = new TextField(student.getContactNumber());
        contactField.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        contactBox.getChildren().addAll(contactLabel, contactField);
        
        // Sex Field
        VBox sexBox = new VBox(5);
        Label sexLabel = new Label("Sex *");
        sexLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        ComboBox<String> sexComboBox = new ComboBox<>();
        sexComboBox.getItems().addAll("Male", "Female");
        sexComboBox.setValue(student.getSex());
        sexComboBox.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 5;");
        sexComboBox.setPrefWidth(200);
        sexBox.getChildren().addAll(sexLabel, sexComboBox);
        
        // LRN Field
        VBox lrnBox = new VBox(5);
        Label lrnLabel = new Label("LRN");
        lrnLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TextField lrnField = new TextField(student.getLrn());
        lrnField.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        lrnBox.getChildren().addAll(lrnLabel, lrnField);
        
        // Readonly fields (Grade Level, Strand, Section)
        VBox readonlyBox = new VBox(10);
        readonlyBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label readonlyTitle = new Label("Read-only Information");
        readonlyTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        
        HBox gradeBox = new HBox(10);
        Label gradeLabel = new Label("Grade Level:");
        gradeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        Label gradeValue = new Label(student.getGradeLevel() != null ? String.valueOf(student.getGradeLevel()) : "N/A");
        gradeValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        gradeBox.getChildren().addAll(gradeLabel, gradeValue);
        
        HBox strandBox = new HBox(10);
        Label strandLabel = new Label("Strand:");
        strandLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        Label strandValue = new Label(student.getStrand() != null ? student.getStrand() : "N/A");
        strandValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        strandBox.getChildren().addAll(strandLabel, strandValue);
        
        HBox sectionBox = new HBox(10);
        Label sectionLabel = new Label("Section:");
        sectionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        Label sectionValue = new Label(section.getName());
        sectionValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        sectionBox.getChildren().addAll(sectionLabel, sectionValue);
        
        readonlyBox.getChildren().addAll(readonlyTitle, gradeBox, strandBox, sectionBox);
        
        content.getChildren().addAll(nameBox, contactBox, sexBox, lrnBox, readonlyBox);
        dialogPane.setContent(content);
        
        // Customize OK button
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Save");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5;");
        
        // Handle save
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Validate
                if (nameField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Name is required");
                    alert.setContentText("Please enter a student name.");
                    alert.showAndWait();
                    return null;
                }
                
                if (sexComboBox.getValue() == null || sexComboBox.getValue().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Sex is required");
                    alert.setContentText("Please select a sex.");
                    alert.showAndWait();
                    return null;
                }
                
                // Update student
                okButton.setDisable(true);
                okButton.setText("Saving...");
                
                new Thread(() -> {
                    try {
                        StudentDto updatedStudent = studentService.updateStudentForTeacher(
                            student.getId(),
                            nameField.getText().trim(),
                            contactField.getText().trim(),
                            sexComboBox.getValue(),
                            lrnField.getText().trim()
                        );
                        
                        Platform.runLater(() -> {
                            // Update the student in the list
                            int index = studentsList.indexOf(student);
                            if (index >= 0) {
                                studentsList.set(index, updatedStudent);
                                table.refresh();
                            }
                            
                            dialog.close();
                            
                            // Show success toast
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText("Student updated successfully");
                            alert.setContentText("Student information has been updated.");
                            alert.showAndWait();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            okButton.setDisable(false);
                            okButton.setText("Save");
                            
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Failed to update student");
                            alert.setContentText("Error: " + e.getMessage());
                            alert.showAndWait();
                        });
                    }
                }).start();
                
                return ButtonType.OK;
            }
            return buttonType;
        });
        
        dialog.showAndWait();
    }
    
    private void loadAccountSettingsPage() {
        if (dashboardContent == null || currentUser == null) {
            return;
        }
        
        dashboardContent.getChildren().clear();
        
        // Page Title
        Label pageTitle = new Label("Account Settings");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 30 0;");
        dashboardContent.getChildren().add(pageTitle);
        
        // Main container
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        // Profile Picture Section
        VBox profilePictureSection = new VBox(15);
        profilePictureSection.setAlignment(Pos.CENTER);
        profilePictureSection.setPadding(new Insets(20));
        
        Label profilePictureLabel = new Label("Profile Picture");
        profilePictureLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Profile picture display - circular container
        accountSettingsProfileContainer = new StackPane();
        accountSettingsProfileContainer.setPrefWidth(150);
        accountSettingsProfileContainer.setPrefHeight(150);
        accountSettingsProfileContainer.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 75;");
        
        // Profile picture ImageView - circular
        accountSettingsProfileImageView = new ImageView();
        accountSettingsProfileImageView.setFitWidth(150);
        accountSettingsProfileImageView.setFitHeight(150);
        accountSettingsProfileImageView.setPreserveRatio(true);
        accountSettingsProfileImageView.setSmooth(true);
        accountSettingsProfileImageView.setCache(true);
        
        // Update clip when layout bounds change to ensure proper centering
        accountSettingsProfileImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                double radius = Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0;
                Circle clip = new Circle(
                    newBounds.getWidth() / 2.0,
                    newBounds.getHeight() / 2.0,
                    radius
                );
                accountSettingsProfileImageView.setClip(clip);
            }
        });
        
        // Set initial clip
        Circle initialClip = new Circle(75, 75, 75);
        accountSettingsProfileImageView.setClip(initialClip);
        
        // Initials label (shown when no picture)
        accountSettingsInitialsLabel = new Label(getInitials(currentUser.getFullName()));
        accountSettingsInitialsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 48px; -fx-font-weight: bold;");
        
        // Load existing profile picture if available
        boolean hasPicture = false;
        if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getProfilePicture());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), 150, 150, true, true, true);
                    accountSettingsProfileImageView.setImage(image);
                    accountSettingsProfileImageView.setVisible(true);
                    accountSettingsInitialsLabel.setVisible(false);
                    hasPicture = true;
                }
            } catch (Exception e) {
                // If image fails to load, show initials
            }
        }
        
        if (!hasPicture) {
            accountSettingsProfileImageView.setVisible(false);
            accountSettingsInitialsLabel.setVisible(true);
            String color = getColorForName(currentUser.getFullName());
            accountSettingsProfileContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 75;");
            accountSettingsInitialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold;");
        }
        
        accountSettingsProfileContainer.getChildren().addAll(accountSettingsProfileImageView, accountSettingsInitialsLabel);
        
        // Upload button
        Button uploadButton = new Button("📷 Upload Picture");
        uploadButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        uploadButton.setOnAction(e -> handleProfilePictureUpload(accountSettingsProfileImageView));
        
        // File size info
        Label fileSizeLabel = new Label("Maximum file size: 3 MB");
        fileSizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        profilePictureSection.getChildren().addAll(profilePictureLabel, accountSettingsProfileContainer, uploadButton, fileSizeLabel);
        
        // Form Section
        VBox formSection = new VBox(20);
        formSection.setPadding(new Insets(20));
        
        // Name Field
        VBox nameFieldBox = new VBox(8);
        Label nameLabel = new Label("Full Name *");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        nameField.setPrefWidth(400);
        nameFieldBox.getChildren().addAll(nameLabel, nameField);
        
        // Password Field (Disabled)
        VBox passwordFieldBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("••••••••");
        passwordField.setDisable(true);
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-opacity: 0.7;");
        passwordField.setPrefWidth(400);
        
        // Show alert when trying to edit password
        passwordField.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Password Change Not Allowed");
            alert.setHeaderText("You do not have permission to edit password");
            alert.setContentText("Please contact the administrator if you need a new password.");
            alert.showAndWait();
        });
        
        Label passwordInfoLabel = new Label("Contact administrator to change password");
        passwordInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        passwordFieldBox.getChildren().addAll(passwordLabel, passwordField, passwordInfoLabel);
        
        // Save Button
        Button saveButton = new Button("💾 Save Changes");
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
        saveButton.setOnAction(e -> handleSaveProfile(nameField.getText().trim(), accountSettingsProfileImageView, saveButton));
        
        formSection.getChildren().addAll(nameFieldBox, passwordFieldBox, saveButton);
        
        mainContainer.getChildren().addAll(profilePictureSection, formSection);
        dashboardContent.getChildren().add(mainContainer);
    }
    
    private File selectedProfilePictureFile = null;
    private ImageView accountSettingsProfileImageView = null;
    private StackPane accountSettingsProfileContainer = null;
    private Label accountSettingsInitialsLabel = null;
    
    private void handleProfilePictureUpload(ImageView profileImageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        Stage stage = (Stage) dashboardContent.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            // Check file size (3 MB = 3 * 1024 * 1024 bytes)
            long fileSize = selectedFile.length();
            long maxSize = 3 * 1024 * 1024;
            
            if (fileSize > maxSize) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Too Large");
                alert.setHeaderText("Profile picture size exceeds limit");
                alert.setContentText("Please select an image file smaller than 3 MB.");
                alert.showAndWait();
                return;
            }
            
            // Validate it's an image file
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    throw new IOException("Invalid image file");
                }
                
                // Store the selected file
                selectedProfilePictureFile = selectedFile;
                
                // Display preview - circular
                Image previewImage = new Image(selectedFile.toURI().toString(), 150, 150, true, true, true);
                profileImageView.setImage(previewImage);
                profileImageView.setVisible(true);
                
                // Hide initials label if visible and update container
                if (accountSettingsInitialsLabel != null) {
                    accountSettingsInitialsLabel.setVisible(false);
                }
                if (accountSettingsProfileContainer != null) {
                    accountSettingsProfileContainer.setStyle("-fx-background-color: transparent; -fx-background-radius: 75;");
                }
                
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Image");
                alert.setHeaderText("Failed to load image");
                alert.setContentText("Please select a valid image file.");
                alert.showAndWait();
            }
        }
    }
    
    private void handleSaveProfile(String fullName, ImageView profileImageView, Button saveButton) {
        // Validate name
        if (fullName == null || fullName.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Full name is required");
            alert.setContentText("Please enter your full name.");
            alert.showAndWait();
            return;
        }
        
        // Disable save button
        if (saveButton != null) {
            saveButton.setDisable(true);
            saveButton.setText("Saving...");
        }
        
        // Save in background thread
        new Thread(() -> {
            try {
                String profilePicturePath = null;
                
                // Handle profile picture upload if a new one was selected
                if (selectedProfilePictureFile != null) {
                    // Create uploads directory if it doesn't exist
                    File uploadsDir = new File("uploads/profile-pictures");
                    if (!uploadsDir.exists()) {
                        uploadsDir.mkdirs();
                    }
                    
                    // Generate unique filename
                    String originalFilename = selectedProfilePictureFile.getName();
                    String extension = "";
                    if (originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String filename = currentUser.getId() + "_" + System.currentTimeMillis() + extension;
                    
                    // Copy file to uploads directory
                    File targetFile = new File(uploadsDir, filename);
                    Files.copy(selectedProfilePictureFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // Delete old profile picture if exists
                    if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
                        File oldFile = new File(currentUser.getProfilePicture());
                        if (oldFile.exists()) {
                            oldFile.delete();
                        }
                    }
                    
                    profilePicturePath = "uploads/profile-pictures/" + filename;
                }
                
                // Update teacher profile using service
                UserDto updatedUser = teacherService.updateTeacherProfile(
                    currentUser.getId(),
                    fullName,
                    profilePicturePath
                );
                
                // Update current user
                currentUser = updatedUser;
                
                // Reset selected file
                selectedProfilePictureFile = null;
                
                // Update header labels and profile picture
                Platform.runLater(() -> {
                    if (userNameLabel != null) {
                        userNameLabel.setText(updatedUser.getFullName());
                    }
                    
                    // Reload profile picture in header
                    loadProfilePicture();
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Profile updated successfully");
                    alert.setContentText("Your profile has been updated.");
                    alert.showAndWait();
                    
                    // Reload account settings page to show updated info
                    loadAccountSettingsPage();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to update profile");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                    
                    // Re-enable save button
                    if (saveButton != null) {
                        saveButton.setDisable(false);
                        saveButton.setText("💾 Save Changes");
                    }
                });
            }
        }).start();
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
    
    private TableView<StudentDto> createStudentsTable(List<StudentDto> students, Section section) {
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
        
        // Action Column with Edit button
        TableColumn<StudentDto, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setStyle("-fx-font-size: 13px;");
        actionCol.setCellFactory(param -> new TableCell<StudentDto, Void>() {
            private final Button editButton = new Button("✏️");
            {
                editButton.setStyle(
                    "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12px; " +
                    "-fx-padding: 5 10; " +
                    "-fx-background-radius: 5; " +
                    "-fx-cursor: hand;"
                );
                editButton.setOnAction(event -> {
                    StudentDto student = getTableView().getItems().get(getIndex());
                    showEditStudentModal(student, section, table, students);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
        
        @SuppressWarnings("unchecked")
        TableColumn<StudentDto, ?>[] columns = new TableColumn[] {
            nameCol, lrnCol, gradeCol, strandCol, sectionCol, sexCol, contactCol, actionCol
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


