package com.enrollment.system.controller;

import com.enrollment.system.dto.UserDto;
import com.enrollment.system.model.Subject;
import com.enrollment.system.model.Section;
import com.enrollment.system.model.Strand;
import com.enrollment.system.service.TeacherService;
import com.enrollment.system.service.SubjectService;
import com.enrollment.system.service.SectionService;
import com.enrollment.system.service.StrandService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AssignTeacherSubjectsSectionsController implements Initializable {
    
    // Assignment record for table display
    public static class AssignmentRecord {
        private Subject subject;
        private Section section;
        
        public AssignmentRecord(Subject subject, Section section) {
            this.subject = subject;
            this.section = section;
        }
        
        public Subject getSubject() { return subject; }
        public Section getSection() { return section; }
        public String getSubjectName() { return subject != null ? subject.getName() : ""; }
        public String getSectionName() { return section != null ? section.getName() : ""; }
        public String getStrand() { return section != null ? section.getStrand() : ""; }
        public Integer getGrade() { return section != null ? section.getGradeLevel() : null; }
    }
    
    @FXML
    private Label teacherNameLabel;
    
    @FXML
    private TableView<AssignmentRecord> assignmentsTable;
    
    @FXML
    private TableColumn<AssignmentRecord, String> subjectColumn;
    
    @FXML
    private TableColumn<AssignmentRecord, String> sectionColumn;
    
    @FXML
    private TableColumn<AssignmentRecord, String> strandColumn;
    
    @FXML
    private TableColumn<AssignmentRecord, Integer> gradeColumn;
    
    @FXML
    private TableColumn<AssignmentRecord, String> actionsColumn;
    
    @FXML
    private Label assignmentsCountLabel;
    
    @FXML
    private ComboBox<Subject> subjectComboBox;
    
    @FXML
    private ComboBox<Section> sectionComboBox;
    
    @FXML
    private ComboBox<String> strandComboBox;
    
    @FXML
    private ComboBox<Integer> gradeComboBox;
    
    @FXML
    private Button addSubjectButton;
    
    @FXML
    private Button editSubjectButton;
    
    @FXML
    private Button deleteSubjectButton;
    
    @FXML
    private Button assignButton;
    
    @FXML
    private Button cancelButton;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private SectionService sectionService;
    
    @Autowired
    private StrandService strandService;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    private UserDto teacher;
    private ObservableList<Subject> allSubjects;
    private ObservableList<Section> allSections;
    private ObservableList<String> allStrands;
    private ObservableList<AssignmentRecord> assignments;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        subjectColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubjectName()));
        sectionColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSectionName()));
        strandColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStrand()));
        gradeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getGrade()).asObject());
        
        // Actions column with remove button
        actionsColumn.setCellFactory(column -> new TableCell<AssignmentRecord, String>() {
            private final Button removeButton = new Button("Remove");
            
            {
                removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                removeButton.setOnAction(e -> {
                    AssignmentRecord record = getTableView().getItems().get(getIndex());
                    handleRemoveAssignment(record);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        // Set up button actions
        addSubjectButton.setOnAction(e -> handleAddSubject());
        editSubjectButton.setOnAction(e -> handleEditSubject());
        deleteSubjectButton.setOnAction(e -> handleDeleteSelectedSubject());
        assignButton.setOnAction(e -> handleAssign());
        cancelButton.setOnAction(e -> handleCancel());
        
        // Set up combo box cell factories for Subject
        if (subjectComboBox != null) {
            subjectComboBox.setCellFactory(listView -> new ListCell<Subject>() {
                @Override
                protected void updateItem(Subject subject, boolean empty) {
                    super.updateItem(subject, empty);
                    if (empty || subject == null) {
                        setText(null);
                    } else {
                        String display = subject.getName() != null ? subject.getName() : "";
                        if (subject.getGradeLevel() != null) {
                            display += " (Grade " + subject.getGradeLevel() + ")";
                        }
                        setText(display);
                    }
                }
            });
            
            subjectComboBox.setButtonCell(new ListCell<Subject>() {
                @Override
                protected void updateItem(Subject subject, boolean empty) {
                    super.updateItem(subject, empty);
                    if (empty || subject == null) {
                        setText(null);
                    } else {
                        String display = subject.getName() != null ? subject.getName() : "";
                        if (subject.getGradeLevel() != null) {
                            display += " (Grade " + subject.getGradeLevel() + ")";
                        }
                        setText(display);
                    }
                }
            });
        }
        
        // Set up combo box cell factories for Section
        if (sectionComboBox != null) {
            sectionComboBox.setCellFactory(listView -> new ListCell<Section>() {
                @Override
                protected void updateItem(Section section, boolean empty) {
                    super.updateItem(section, empty);
                    if (empty || section == null) {
                        setText(null);
                    } else {
                        String name = section.getName() != null ? section.getName() : "";
                        String strand = section.getStrand() != null ? section.getStrand() : "";
                        Integer grade = section.getGradeLevel();
                        setText(name + " (" + strand + " - Grade " + grade + ")");
                    }
                }
            });
            
            sectionComboBox.setButtonCell(new ListCell<Section>() {
                @Override
                protected void updateItem(Section section, boolean empty) {
                    super.updateItem(section, empty);
                    if (empty || section == null) {
                        setText(null);
                    } else {
                        String name = section.getName() != null ? section.getName() : "";
                        String strand = section.getStrand() != null ? section.getStrand() : "";
                        Integer grade = section.getGradeLevel();
                        setText(name + " (" + strand + " - Grade " + grade + ")");
                    }
                }
            });
        }
        
        // Initialize grade ComboBox with values 11 and 12
        if (gradeComboBox != null) {
            gradeComboBox.setItems(FXCollections.observableArrayList(11, 12));
        }
        
        // Update sections when strand or grade changes
        strandComboBox.setOnAction(e -> updateSectionsByStrandAndGrade());
        gradeComboBox.setOnAction(e -> updateSectionsByStrandAndGrade());
        
        // Update grade ComboBox when section changes (auto-select matching grade)
        sectionComboBox.setOnAction(e -> updateGradeFromSection());
        
        // Initialize assignments list
        assignments = FXCollections.observableArrayList();
        assignmentsTable.setItems(assignments);
        
        // Initialize empty lists to prevent null pointer exceptions
        allSubjects = FXCollections.observableArrayList();
        allSections = FXCollections.observableArrayList();
        allStrands = FXCollections.observableArrayList();
        
        // Load data after a short delay to ensure UI is ready
        Platform.runLater(() -> {
            // Ensure subjects have proper isActive status first
            try {
                subjectService.ensureAllSubjectsHaveActiveStatus();
            } catch (Exception e) {
                System.err.println("Warning: Could not ensure subject active status: " + e.getMessage());
            }
            
            loadSubjects();
            loadSections();
            loadStrands();
        });
    }
    
    public void setTeacher(UserDto teacher) {
        this.teacher = teacher;
        if (teacherNameLabel != null && teacher != null) {
            teacherNameLabel.setText("Teacher: " + teacher.getFullName() + " (" + teacher.getUsername() + ")");
        }
        
        // Load teacher's current assignments
        if (teacher != null) {
            loadTeacherAssignments();
        }
    }
    
    private void loadSubjects() {
        // Check if service is injected
        if (subjectService == null) {
            System.err.println("ERROR: subjectService is null! Spring injection may have failed.");
            Platform.runLater(() -> {
                showError("Subject service not available. Please restart the application.");
            });
            return;
        }
        
        // Run on background thread to avoid blocking UI
        new Thread(() -> {
            try {
                System.out.println("Loading subjects from database...");
                
                // Try to get all subjects first, then filter active ones
                // This handles cases where isActive might be NULL in SQLite
                List<Subject> allSubjectsList = subjectService.getAllSubjects();
                System.out.println("Found " + allSubjectsList.size() + " total subjects in database");
                
                // Filter active subjects (handle NULL isActive values)
                List<Subject> subjects = allSubjectsList.stream()
                    .filter(s -> s.getIsActive() != null && s.getIsActive())
                    .collect(Collectors.toList());
                
                System.out.println("Found " + subjects.size() + " active subjects after filtering");
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        if (subjects == null || subjects.isEmpty()) {
                            System.out.println("WARNING: No active subjects found in database!");
                            allSubjects = FXCollections.observableArrayList();
                            
                            // Show helpful message to user
                            System.out.println("INFO: You can add subjects using the 'Add Subject' button");
                        } else {
                            allSubjects = FXCollections.observableArrayList(subjects);
                            
                            // Sort by creation date descending (newest first), then by grade level, then by name
                            allSubjects.sort((s1, s2) -> {
                                // First, sort by creation date (newest first)
                                if (s1.getCreatedAt() != null && s2.getCreatedAt() != null) {
                                    int dateCompare = s2.getCreatedAt().compareTo(s1.getCreatedAt());
                                    if (dateCompare != 0) return dateCompare;
                                } else if (s1.getCreatedAt() != null) {
                                    return -1; // s1 has date, s2 doesn't - s1 comes first
                                } else if (s2.getCreatedAt() != null) {
                                    return 1; // s2 has date, s1 doesn't - s2 comes first
                                }
                                
                                // If dates are equal or both null, sort by grade level
                                int gradeCompare = Integer.compare(
                                    s1.getGradeLevel() != null ? s1.getGradeLevel() : 0,
                                    s2.getGradeLevel() != null ? s2.getGradeLevel() : 0
                                );
                                if (gradeCompare != 0) return gradeCompare;
                                
                                // Finally, sort by name
                                String name1 = s1.getName() != null ? s1.getName() : "";
                                String name2 = s2.getName() != null ? s2.getName() : "";
                                return name1.compareToIgnoreCase(name2);
                            });
                        }
                        
                        if (subjectComboBox != null) {
                            subjectComboBox.setItems(allSubjects);
                            System.out.println("Set " + allSubjects.size() + " subjects into dropdown ComboBox");
                            System.out.println("Subject ComboBox items count: " + subjectComboBox.getItems().size());
                            
                            if (allSubjects.isEmpty()) {
                                System.out.println("WARNING: No subjects found in database!");
                            } else {
                                // Print sample subjects for debugging
                                System.out.println("Sample subjects loaded:");
                                int count = 0;
                                for (Subject s : allSubjects) {
                                    if (count >= 5) break;
                                    System.out.println("  - " + s.getName() + " (Grade " + s.getGradeLevel() + ")");
                                    count++;
                                }
                            }
                        } else {
                            System.out.println("ERROR: subjectComboBox is null!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Exception in loadSubjects Platform.runLater: " + e.getMessage());
                        showError("Error updating subjects dropdown: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Exception in loadSubjects background thread: " + e.getMessage());
                Platform.runLater(() -> showError("Error loading subjects: " + e.getMessage()));
            }
        }).start();
    }
    
    private void loadSections() {
        // Run on background thread to avoid blocking UI
        new Thread(() -> {
            try {
                System.out.println("Loading sections from database...");
                List<Section> sections = sectionService.getActiveSections();
                System.out.println("Found " + sections.size() + " active sections in database");
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        if (sections == null || sections.isEmpty()) {
                            System.out.println("WARNING: No sections returned from service!");
                            allSections = FXCollections.observableArrayList();
                        } else {
                            allSections = FXCollections.observableArrayList(sections);
                            
                            // Sort by strand, then grade, then name
                            allSections.sort((s1, s2) -> {
                                String strand1 = s1.getStrand() != null ? s1.getStrand() : "";
                                String strand2 = s2.getStrand() != null ? s2.getStrand() : "";
                                int strandCompare = strand1.compareToIgnoreCase(strand2);
                                if (strandCompare != 0) return strandCompare;
                                
                                int gradeCompare = Integer.compare(
                                    s1.getGradeLevel() != null ? s1.getGradeLevel() : 0,
                                    s2.getGradeLevel() != null ? s2.getGradeLevel() : 0
                                );
                                if (gradeCompare != 0) return gradeCompare;
                                
                                String name1 = s1.getName() != null ? s1.getName() : "";
                                String name2 = s2.getName() != null ? s2.getName() : "";
                                return name1.compareToIgnoreCase(name2);
                            });
                        }
                        
                        // Initially show all sections
                        if (sectionComboBox != null) {
                            sectionComboBox.setItems(allSections);
                            System.out.println("Set " + allSections.size() + " sections into dropdown ComboBox");
                            System.out.println("Section ComboBox items count: " + sectionComboBox.getItems().size());
                            
                            if (allSections.isEmpty()) {
                                System.out.println("WARNING: No sections found in database!");
                            } else {
                                // Print first few sections for debugging
                                System.out.println("Sample sections loaded:");
                                for (int i = 0; i < Math.min(3, allSections.size()); i++) {
                                    Section s = allSections.get(i);
                                    System.out.println("  - " + s.getName() + " (" + s.getStrand() + " - Grade " + s.getGradeLevel() + ")");
                                }
                            }
                        } else {
                            System.out.println("ERROR: sectionComboBox is null!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Exception in loadSections Platform.runLater: " + e.getMessage());
                        showError("Error updating sections dropdown: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Exception in loadSections background thread: " + e.getMessage());
                Platform.runLater(() -> showError("Error loading sections: " + e.getMessage()));
            }
        }).start();
    }
    
    private void loadStrands() {
        // Run on background thread to avoid blocking UI
        new Thread(() -> {
            try {
                System.out.println("Loading strands from database...");
                List<Strand> strands = strandService.getActiveStrands();
                System.out.println("Found " + strands.size() + " active strands in database");
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        if (strands == null || strands.isEmpty()) {
                            System.out.println("WARNING: No strands returned from service!");
                            allStrands = FXCollections.observableArrayList();
                        } else {
                            allStrands = FXCollections.observableArrayList(
                                strands.stream()
                                    .map(Strand::getName)
                                    .filter(name -> name != null && !name.trim().isEmpty())
                                    .sorted()
                                    .collect(Collectors.toList())
                            );
                        }
                        
                        if (strandComboBox != null) {
                            strandComboBox.setItems(allStrands);
                            System.out.println("Set " + allStrands.size() + " strands into dropdown ComboBox");
                            System.out.println("Strand ComboBox items count: " + strandComboBox.getItems().size());
                            
                            if (allStrands.isEmpty()) {
                                System.out.println("WARNING: No strands found in database!");
                            } else {
                                // Print all strands for debugging
                                System.out.println("Strands loaded:");
                                for (String strand : allStrands) {
                                    System.out.println("  - " + strand);
                                }
                            }
                        } else {
                            System.out.println("ERROR: strandComboBox is null!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Exception in loadStrands Platform.runLater: " + e.getMessage());
                        showError("Error updating strands dropdown: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Exception in loadStrands background thread: " + e.getMessage());
                Platform.runLater(() -> showError("Error loading strands: " + e.getMessage()));
            }
        }).start();
    }
    
    private void updateSectionsByStrandAndGrade() {
        if (allSections == null) {
            return; // Sections not loaded yet
        }
        
        String selectedStrand = strandComboBox.getValue();
        Integer selectedGrade = gradeComboBox.getValue();
        
        // Filter sections by strand and grade
        ObservableList<Section> filtered = FXCollections.observableArrayList(
            allSections.stream()
                .filter(s -> {
                    boolean matchesStrand = selectedStrand == null || selectedStrand.isEmpty() || 
                                          (s.getStrand() != null && selectedStrand.equals(s.getStrand()));
                    boolean matchesGrade = selectedGrade == null || 
                                         (s.getGradeLevel() != null && selectedGrade.equals(s.getGradeLevel()));
                    return matchesStrand && matchesGrade;
                })
                .collect(Collectors.toList())
        );
        sectionComboBox.setItems(filtered);
    }
    
    private void updateGradeFromSection() {
        Section selectedSection = sectionComboBox.getValue();
        if (selectedSection != null && selectedSection.getGradeLevel() != null) {
            // Auto-select the grade in the ComboBox
            gradeComboBox.setValue(selectedSection.getGradeLevel());
        }
    }
    
    private void loadTeacherAssignments() {
        if (teacher == null) {
            return;
        }
        
        try {
            // Load teacher's subjects and sections
            List<Subject> teacherSubjects = teacherService.getTeacherSubjects(teacher.getId());
            List<Section> teacherSections = teacherService.getTeacherSections(teacher.getId());
            
            // Create assignment records (combine subjects with sections)
            // For simplicity, we'll show all subject-section combinations
            // In a real scenario, you might want a more specific assignment model
            assignments.clear();
            
            // Create assignment records
            // For each subject-section combination, create an assignment record
            // This is a simplified view - in reality you might want a TeacherAssignment entity
            for (Subject subject : teacherSubjects) {
                for (Section section : teacherSections) {
                    assignments.add(new AssignmentRecord(subject, section));
                }
            }
            
            updateAssignmentsCount();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading teacher assignments: " + e.getMessage());
        }
    }
    
    private void updateAssignmentsCount() {
        int count = assignments.size();
        assignmentsCountLabel.setText("Total Assignments: " + count);
    }
    
    private void handleAddSubject() {
        // Check if service is available
        if (subjectService == null) {
            showError("Subject service not available. Please restart the application.");
            return;
        }
        
        // Create simple dialog to add new subject
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Subject");
        dialog.setHeaderText("Enter subject details");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Subject Name");
        ComboBox<Integer> gradeLevelCombo = new ComboBox<>(FXCollections.observableArrayList(11, 12));
        gradeLevelCombo.setValue(11);
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);
        
        grid.add(new Label("Subject Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Grade Level:"), 0, 1);
        grid.add(gradeLevelCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Disable add button initially
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        // Enable add button when name is entered
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });
        
        Platform.runLater(() -> nameField.requestFocus());
        
        // Convert result when add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return nameField.getText().trim() + "|" + gradeLevelCombo.getValue() + "|" + descriptionArea.getText().trim();
            }
            return null;
        });
        
        // Handle result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String[] parts = data.split("\\|", 3);
            String subjectName = parts[0];
            Integer gradeLevel = Integer.parseInt(parts[1]);
            String description = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;
            
            if (subjectName.isEmpty()) {
                showError("Subject name is required");
                return;
            }
            
            // Run in background thread with proper transaction management
            new Thread(() -> {
                try {
                    // Use TransactionTemplate to ensure proper transaction management in background thread
                    // This is critical - @Transactional doesn't work in background threads without this
                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                    transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                    transactionTemplate.setTimeout(30);
                    
                    // Execute subject creation directly within transaction (like DataInitializer does)
                    // This avoids nested transaction issues and ensures @PrePersist fires
                    Subject newSubject = transactionTemplate.execute(status -> {
                        // Validate
                        if (subjectName == null || subjectName.trim().isEmpty()) {
                            throw new RuntimeException("Subject name is required");
                        }
                        if (gradeLevel == null || (gradeLevel != 11 && gradeLevel != 12)) {
                            throw new RuntimeException("Grade level must be 11 or 12");
                        }
                        
                        // Check for duplicates
                        if (subjectService.getSubjectRepository().existsByNameAndGradeLevel(subjectName.trim(), gradeLevel)) {
                            throw new RuntimeException("Subject '" + subjectName + "' already exists for Grade " + gradeLevel);
                        }
                        
                        // Create subject exactly like DataInitializer - let @PrePersist handle timestamps
                        Subject subject = new Subject();
                        subject.setName(subjectName.trim());
                        subject.setGradeLevel(gradeLevel);
                        subject.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
                        subject.setIsActive(true);
                        subject.setIsCustom(true); // Manually added subjects are custom
                        
                        // Save - @PrePersist will set timestamps automatically
                        Subject saved = subjectService.getSubjectRepository().save(subject);
                        
                        // Verify timestamps were set
                        if (saved.getCreatedAt() == null) {
                            // Fallback: set timestamps manually if @PrePersist didn't fire
                            java.time.LocalDateTime now = java.time.LocalDateTime.now();
                            saved.setCreatedAt(now);
                            saved.setUpdatedAt(now);
                            saved = subjectService.getSubjectRepository().save(saved);
                        }
                        
                        return saved;
                    });
                    
                    // Verify subject was created
                    if (newSubject == null || newSubject.getId() == null) {
                        throw new RuntimeException("Subject creation failed - no subject returned");
                    }
                    
                    final Subject createdSubject = newSubject;
                    
                    // Refresh dropdown on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            // Reload subjects from database
                            List<Subject> allSubjectsList = subjectService.getAllSubjects();
                            List<Subject> updatedSubjects = allSubjectsList.stream()
                                .filter(s -> s.getIsActive() != null && s.getIsActive())
                                .collect(Collectors.toList());
                            
                            allSubjects = FXCollections.observableArrayList(updatedSubjects);
                            
                            // Sort by creation date descending (newest first), then by grade level, then by name
                            allSubjects.sort((s1, s2) -> {
                                // First, sort by creation date (newest first)
                                if (s1.getCreatedAt() != null && s2.getCreatedAt() != null) {
                                    int dateCompare = s2.getCreatedAt().compareTo(s1.getCreatedAt());
                                    if (dateCompare != 0) return dateCompare;
                                } else if (s1.getCreatedAt() != null) {
                                    return -1; // s1 has date, s2 doesn't - s1 comes first
                                } else if (s2.getCreatedAt() != null) {
                                    return 1; // s2 has date, s1 doesn't - s2 comes first
                                }
                                
                                // If dates are equal or both null, sort by grade level
                                int gradeCompare = Integer.compare(
                                    s1.getGradeLevel() != null ? s1.getGradeLevel() : 0,
                                    s2.getGradeLevel() != null ? s2.getGradeLevel() : 0
                                );
                                if (gradeCompare != 0) return gradeCompare;
                                
                                // Finally, sort by name
                                String name1 = s1.getName() != null ? s1.getName() : "";
                                String name2 = s2.getName() != null ? s2.getName() : "";
                                return name1.compareToIgnoreCase(name2);
                            });
                            
                            // Update ComboBox - refresh list but don't auto-select
                            if (subjectComboBox != null) {
                                subjectComboBox.setItems(allSubjects);
                                subjectComboBox.setValue(null); // Clear selection to show "Select a subject"
                                System.out.println("✓ Subject added successfully: " + createdSubject.getName());
                            }
                            
                            showSuccess("Subject '" + createdSubject.getName() + "' added successfully!");
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showError("Error refreshing subjects dropdown: " + ex.getMessage());
                        }
                    });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String errorMessage = ex.getMessage();
                    Platform.runLater(() -> {
                        if (errorMessage != null && errorMessage.contains("already exists")) {
                            showError("Subject '" + subjectName + "' already exists for Grade " + gradeLevel);
                        } else if (errorMessage != null && errorMessage.contains("NOT NULL")) {
                            showError("Error adding subject: Database constraint violation. Please check all required fields are provided.");
                        } else {
                            showError("Error adding subject: " + (errorMessage != null ? errorMessage : "Unknown error occurred"));
                        }
                    });
                }
            }).start();
        });
    }
    
    private void handleAssign() {
        if (teacher == null) {
            showError("No teacher selected");
            return;
        }
        
        Subject selectedSubject = subjectComboBox.getValue();
        Section selectedSection = sectionComboBox.getValue();
        
        if (selectedSubject == null) {
            showError("Please select a subject");
            return;
        }
        
        if (selectedSection == null) {
            showError("Please select a section");
            return;
        }
        
        // Check if this assignment already exists
        boolean alreadyExists = assignments.stream()
            .anyMatch(a -> a.getSubject().getId().equals(selectedSubject.getId()) &&
                          a.getSection().getId().equals(selectedSection.getId()));
        
        if (alreadyExists) {
            showError("This assignment already exists");
            return;
        }
        
        // Add to local list first (optimistic update)
        AssignmentRecord newRecord = new AssignmentRecord(selectedSubject, selectedSection);
        assignments.add(newRecord);
        updateAssignmentsCount();
        
        // Clear selections
        subjectComboBox.setValue(null);
        sectionComboBox.setValue(null);
        strandComboBox.setValue(null);
        gradeComboBox.setValue(null);
        
        // Save to database in background
        saveAssignments();
    }
    
    private void handleRemoveAssignment(AssignmentRecord record) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Remove Assignment");
        confirmAlert.setHeaderText("Remove Assignment");
        confirmAlert.setContentText("Are you sure you want to remove this assignment?\n\n" +
                                   "Subject: " + record.getSubjectName() + "\n" +
                                   "Section: " + record.getSectionName());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                assignments.remove(record);
                updateAssignmentsCount();
                saveAssignments();
            }
        });
    }
    
    private void saveAssignments() {
        if (teacher == null) {
            return;
        }
        
        // Disable assign button
        assignButton.setDisable(true);
        assignButton.setText("Saving...");
        
        // Run database operation in background thread
        new Thread(() -> {
            try {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                transactionTemplate.setTimeout(30);
                
                int maxRetries = 5;
                int retryDelay = 200;
                Exception lastException = null;
                
                for (int attempt = 0; attempt < maxRetries; attempt++) {
                    try {
                        transactionTemplate.execute(status -> {
                            // Get unique subjects and sections from assignments
                            Set<Long> subjectIds = assignments.stream()
                                .map(a -> a.getSubject().getId())
                                .collect(Collectors.toSet());
                            
                            Set<Long> sectionIds = assignments.stream()
                                .map(a -> a.getSection().getId())
                                .collect(Collectors.toSet());
                            
                            // Validate maximum 8 subjects
                            if (subjectIds.size() > 8) {
                                throw new RuntimeException("A teacher can have a maximum of 8 subjects");
                            }
                            
                            // Assign subjects
                            teacherService.assignSubjects(teacher.getId(), new ArrayList<>(subjectIds));
                            
                            // Assign sections
                            teacherService.assignSections(teacher.getId(), new ArrayList<>(sectionIds));
                            
                            return null;
                        });
                        
                        lastException = null;
                        break;
                        
                    } catch (DataAccessException e) {
                        lastException = e;
                        String errorMessage = e.getMessage();
                        
                        if (errorMessage != null && (errorMessage.contains("SQLITE_BUSY") || 
                            errorMessage.contains("database is locked") || 
                            errorMessage.contains("database locked") ||
                            errorMessage.contains("SQLITE_BUSY_SNAPSHOT"))) {
                            
                            if (attempt < maxRetries - 1) {
                                try {
                                    Thread.sleep(retryDelay * (attempt + 1));
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    Platform.runLater(() -> {
                                        assignButton.setDisable(false);
                                        assignButton.setText("Assign");
                                        showError("Operation was interrupted. Please try again.");
                                    });
                                    return;
                                }
                                continue;
                            }
                        }
                        
                        throw e;
                    }
                }
                
                if (lastException != null) {
                    throw lastException;
                }
                
                Platform.runLater(() -> {
                    assignButton.setDisable(false);
                    assignButton.setText("Assign");
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    assignButton.setDisable(false);
                    assignButton.setText("Assign");
                    showError("Error saving assignments: " + e.getMessage());
                    // Reload assignments to sync with database
                    loadTeacherAssignments();
                });
            }
        }).start();
    }
    
    private void handleDeleteSelectedSubject() {
        Subject selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null) {
            showError("Please select a subject to delete.");
            return;
        }
        
        handleDeleteSubject(selectedSubject);
    }
    
    private void handleDeleteSubject(Subject subject) {
        if (subject == null) {
            return;
        }
        
        // Confirm deletion - all subjects can be deleted
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Subject");
        confirmAlert.setHeaderText("Delete Subject");
        confirmAlert.setContentText("Are you sure you want to delete this subject?\n\n" +
                                   "Subject: " + subject.getName() + " (Grade " + subject.getGradeLevel() + ")\n\n" +
                                   "This action cannot be undone.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete in background thread
                new Thread(() -> {
                    try {
                        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                        transactionTemplate.setTimeout(30);
                        
                        transactionTemplate.execute(status -> {
                            subjectService.deleteSubject(subject.getId());
                            return null;
                        });
                        
                        // Refresh dropdown on JavaFX thread
                        Platform.runLater(() -> {
                            try {
                                // Reload subjects
                                List<Subject> allSubjectsList = subjectService.getAllSubjects();
                                List<Subject> updatedSubjects = allSubjectsList.stream()
                                    .filter(s -> s.getIsActive() != null && s.getIsActive())
                                    .collect(Collectors.toList());
                                
                                allSubjects = FXCollections.observableArrayList(updatedSubjects);
                                
                                // Sort by creation date descending (newest first), then by grade level, then by name
                                allSubjects.sort((s1, s2) -> {
                                    // First, sort by creation date (newest first)
                                    if (s1.getCreatedAt() != null && s2.getCreatedAt() != null) {
                                        int dateCompare = s2.getCreatedAt().compareTo(s1.getCreatedAt());
                                        if (dateCompare != 0) return dateCompare;
                                    } else if (s1.getCreatedAt() != null) {
                                        return -1; // s1 has date, s2 doesn't - s1 comes first
                                    } else if (s2.getCreatedAt() != null) {
                                        return 1; // s2 has date, s1 doesn't - s2 comes first
                                    }
                                    
                                    // If dates are equal or both null, sort by grade level
                                    int gradeCompare = Integer.compare(
                                        s1.getGradeLevel() != null ? s1.getGradeLevel() : 0,
                                        s2.getGradeLevel() != null ? s2.getGradeLevel() : 0
                                    );
                                    if (gradeCompare != 0) return gradeCompare;
                                    
                                    // Finally, sort by name
                                    String name1 = s1.getName() != null ? s1.getName() : "";
                                    String name2 = s2.getName() != null ? s2.getName() : "";
                                    return name1.compareToIgnoreCase(name2);
                                });
                                
                                // Update ComboBox
                                if (subjectComboBox != null) {
                                    subjectComboBox.setItems(allSubjects);
                                    subjectComboBox.setValue(null); // Clear selection
                                }
                                
                                showSuccess("Subject '" + subject.getName() + "' deleted successfully!");
                                
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showError("Error refreshing subjects dropdown: " + ex.getMessage());
                            }
                        });
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            showError("Error deleting subject: " + ex.getMessage());
                        });
                    }
                }).start();
            }
        });
    }
    
    private void handleEditSubject() {
        Subject selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null) {
            showError("Please select a subject to edit.");
            return;
        }
        
        // Check if service is available
        if (subjectService == null) {
            showError("Subject service not available. Please restart the application.");
            return;
        }
        
        // Create dialog to edit subject
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Subject");
        dialog.setHeaderText("Edit subject details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(selectedSubject.getName());
        nameField.setPromptText("Subject Name");
        ComboBox<Integer> gradeLevelCombo = new ComboBox<>(FXCollections.observableArrayList(11, 12));
        gradeLevelCombo.setValue(selectedSubject.getGradeLevel());
        TextArea descriptionArea = new TextArea(selectedSubject.getDescription() != null ? selectedSubject.getDescription() : "");
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);
        
        grid.add(new Label("Subject Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Grade Level:"), 0, 1);
        grid.add(gradeLevelCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Disable save button initially
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Enable save button when name is entered
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });
        
        Platform.runLater(() -> nameField.requestFocus());
        
        // Convert result when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return nameField.getText().trim() + "|" + gradeLevelCombo.getValue() + "|" + descriptionArea.getText().trim();
            }
            return null;
        });
        
        // Handle result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String[] parts = data.split("\\|", 3);
            String subjectName = parts[0];
            Integer gradeLevel = Integer.parseInt(parts[1]);
            String description = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;
            
            if (subjectName.isEmpty()) {
                showError("Subject name is required");
                return;
            }
            
            // Update in background thread
            new Thread(() -> {
                try {
                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                    transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                    transactionTemplate.setTimeout(30);
                    
                    Subject updatedSubject = transactionTemplate.execute(status -> {
                        return subjectService.updateSubject(selectedSubject.getId(), subjectName, gradeLevel, description);
                    });
                    
                    if (updatedSubject == null) {
                        throw new RuntimeException("Subject update failed");
                    }
                    
                    final Subject finalUpdatedSubject = updatedSubject;
                    
                    // Refresh dropdown on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            // Reload subjects
                            List<Subject> allSubjectsList = subjectService.getAllSubjects();
                            List<Subject> updatedSubjects = allSubjectsList.stream()
                                .filter(s -> s.getIsActive() != null && s.getIsActive())
                                .collect(Collectors.toList());
                            
                            allSubjects = FXCollections.observableArrayList(updatedSubjects);
                            
                            // Sort by creation date descending (newest first), then by grade level, then by name
                            allSubjects.sort((s1, s2) -> {
                                // First, sort by creation date (newest first)
                                if (s1.getCreatedAt() != null && s2.getCreatedAt() != null) {
                                    int dateCompare = s2.getCreatedAt().compareTo(s1.getCreatedAt());
                                    if (dateCompare != 0) return dateCompare;
                                } else if (s1.getCreatedAt() != null) {
                                    return -1; // s1 has date, s2 doesn't - s1 comes first
                                } else if (s2.getCreatedAt() != null) {
                                    return 1; // s2 has date, s1 doesn't - s2 comes first
                                }
                                
                                // If dates are equal or both null, sort by grade level
                                int gradeCompare = Integer.compare(
                                    s1.getGradeLevel() != null ? s1.getGradeLevel() : 0,
                                    s2.getGradeLevel() != null ? s2.getGradeLevel() : 0
                                );
                                if (gradeCompare != 0) return gradeCompare;
                                
                                // Finally, sort by name
                                String name1 = s1.getName() != null ? s1.getName() : "";
                                String name2 = s2.getName() != null ? s2.getName() : "";
                                return name1.compareToIgnoreCase(name2);
                            });
                            
                            // Update ComboBox
                            if (subjectComboBox != null) {
                                subjectComboBox.setItems(allSubjects);
                                
                                // Select the updated subject
                                Subject subjectToSelect = allSubjects.stream()
                                    .filter(s -> s.getId() != null && s.getId().equals(finalUpdatedSubject.getId()))
                                    .findFirst()
                                    .orElse(null);
                                
                                if (subjectToSelect != null) {
                                    subjectComboBox.setValue(subjectToSelect);
                                }
                            }
                            
                            showSuccess("Subject '" + finalUpdatedSubject.getName() + "' updated successfully!");
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showError("Error refreshing subjects dropdown: " + ex.getMessage());
                        }
                    });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String errorMessage = ex.getMessage();
                    Platform.runLater(() -> {
                        if (errorMessage != null && errorMessage.contains("already exists")) {
                            showError("Subject '" + subjectName + "' already exists for Grade " + gradeLevel);
                        } else {
                            showError("Error updating subject: " + (errorMessage != null ? errorMessage : "Unknown error occurred"));
                        }
                    });
                }
            }).start();
        });
    }
    
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
