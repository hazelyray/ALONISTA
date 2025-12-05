package com.enrollment.system.controller;

import com.enrollment.system.dto.StudentDto;
import com.enrollment.system.model.Section;
import com.enrollment.system.model.Strand;
import com.enrollment.system.service.SectionService;
import com.enrollment.system.service.StrandService;
import com.enrollment.system.service.StudentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class AddStudentController {
    
    @FXML
    private TextField nameField;
    
    @FXML
    private DatePicker birthdatePicker;
    
    @FXML
    private TextField ageField;
    
    @FXML
    private ComboBox<String> sexComboBox;
    
    @FXML
    private TextField addressField;
    
    @FXML
    private TextField contactNumberField;
    
    @FXML
    private TextField parentGuardianNameField;
    
    @FXML
    private TextField parentGuardianContactField;
    
    @FXML
    private ComboBox<String> parentGuardianRelationshipComboBox;
    
    @FXML
    private ComboBox<Integer> gradeLevelComboBox;
    
    @FXML
    private ComboBox<String> strandComboBox;
    
    @FXML
    private ComboBox<Section> sectionComboBox;
    
    @FXML
    private TextField previousSchoolField;
    
    @FXML
    private TextField gwaField;
    
    @FXML
    private TextField lrnField;
    
    @FXML
    private ComboBox<String> enrollmentStatusComboBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SectionService sectionService;
    
    @Autowired
    private StrandService strandService;
    
    @FXML
    public void initialize() {
        // Initialize Sex ComboBox
        sexComboBox.getItems().addAll("Male", "Female");
        
        // Initialize Parent/Guardian Relationship ComboBox
        parentGuardianRelationshipComboBox.getItems().addAll("Father", "Mother", "Guardian", "Other");
        
        // Initialize Grade Level ComboBox
        gradeLevelComboBox.getItems().addAll(11, 12);
        
        // Initialize Strand ComboBox - Load active strands from service
        loadActiveStrands();
        
        // Add listener to strand to update sections when strand changes
        strandComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateSections();
        });
        
        // Initialize Enrollment Status ComboBox (only Enrolled and Pending)
        enrollmentStatusComboBox.getItems().addAll("Enrolled", "Pending");
        
        // Set default values
        enrollmentStatusComboBox.setValue("Pending");
        
        // Add listener to enrollment status to toggle section requirement and clear section if needed
        enrollmentStatusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateSectionRequirement();
            // Clear section selection if status is not "Enrolled"
            if (!"Enrolled".equals(newValue) && sectionComboBox.getValue() != null) {
                sectionComboBox.setValue(null);
            }
        });
        
        // Add listener to grade level to update sections
        gradeLevelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateSections();
        });
        
        // Add listener to birthdate picker to auto-calculate age
        birthdatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int age = calculateAge(newValue);
                ageField.setText(String.valueOf(age));
            }
        });
        
        // Add listener to age field to validate
        ageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ageField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Add listener to GWA field to validate decimal and range
        gwaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                gwaField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    double gwaValue = Double.parseDouble(newValue);
                    if (gwaValue < 75.0 || gwaValue > 100.0) {
                        gwaField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    } else {
                        gwaField.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    // Invalid format, will be handled on focus lost
                }
            } else {
                gwaField.setStyle("");
            }
        });
        
        // Validate GWA on focus lost
        gwaField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // When focus is lost
                String text = gwaField.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        double gwaValue = Double.parseDouble(text);
                        if (gwaValue < 75.0 || gwaValue > 100.0) {
                            gwaField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                        } else {
                            gwaField.setStyle("");
                        }
                    } catch (NumberFormatException e) {
                        gwaField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    }
                }
            }
        });
        
        // Limit LRN to 12 digits only
        lrnField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 12) {
                lrnField.setText(oldValue);
            } else if (!newValue.matches("^\\d*$")) {
                // Only allow digits
                lrnField.setText(oldValue);
            }
        });
        
        // Limit contact number fields to 11 digits
        contactNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 11) {
                contactNumberField.setText(oldValue);
            } else if (!newValue.matches("^\\d*$")) {
                // Only allow digits
                contactNumberField.setText(oldValue);
            }
        });
        
        parentGuardianContactField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 11) {
                parentGuardianContactField.setText(oldValue);
            } else if (!newValue.matches("^\\d*$")) {
                // Only allow digits
                parentGuardianContactField.setText(oldValue);
            }
        });
        
        // Add listener to contact number fields to validate on focus lost
        contactNumberField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // When focus is lost
                String text = contactNumberField.getText().trim();
                if (!text.isEmpty() && !isValidPhilippineContactNumber(text)) {
                    contactNumberField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                } else {
                    contactNumberField.setStyle("");
                }
            }
        });
        
        parentGuardianContactField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // When focus is lost
                String text = parentGuardianContactField.getText().trim();
                if (!text.isEmpty() && !isValidPhilippineContactNumber(text)) {
                    parentGuardianContactField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                } else {
                    parentGuardianContactField.setStyle("");
                }
            }
        });
        
        // Set DatePicker to disable dates that would make student 15 or younger
        birthdatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null) {
                    int age = calculateAge(date);
                    if (age < 16) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffcccc;");
                    }
                }
            }
        });
        
        // Add listener to previous school field to validate abbreviations
        previousSchoolField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // When focus is lost
                String text = previousSchoolField.getText().trim();
                if (!text.isEmpty() && isAllCapitalAbbreviation(text)) {
                    previousSchoolField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                } else {
                    previousSchoolField.setStyle("");
                }
            }
        });
    }
    
    private void loadActiveStrands() {
        try {
            java.util.List<Strand> activeStrands = strandService.getActiveStrands();
            strandComboBox.getItems().clear();
            for (Strand strand : activeStrands) {
                strandComboBox.getItems().add(strand.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to hardcoded values if service fails
            strandComboBox.getItems().addAll("ABM", "HUMSS", "STEM", "GAS", "TVL");
        }
    }
    
    private boolean isValidPhilippineContactNumber(String contactNumber) {
        // Philippine format: 09XXXXXXXXX (exactly 11 digits starting with 09)
        return contactNumber.matches("^09\\d{9}$") && contactNumber.length() == 11;
    }
    
    @FXML
    private void handleStrandChange() {
        updateSections();
    }
    
    private void updateSections() {
        String selectedStrand = strandComboBox.getValue();
        Integer selectedGradeLevel = gradeLevelComboBox.getValue();
        
        sectionComboBox.getItems().clear();
        
        if (selectedStrand != null && selectedGradeLevel != null) {
            java.util.List<Section> sections = sectionService.getActiveSectionsByStrandAndGradeLevel(selectedStrand, selectedGradeLevel);
            sectionComboBox.getItems().addAll(sections);
            
            // Set cell factory to display section names
            sectionComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<Section>() {
                @Override
                protected void updateItem(Section section, boolean empty) {
                    super.updateItem(section, empty);
                    if (empty || section == null) {
                        setText(null);
                    } else {
                        setText(section.getName());
                    }
                }
            });
            
            // Set button cell factory
            sectionComboBox.setButtonCell(new javafx.scene.control.ListCell<Section>() {
                @Override
                protected void updateItem(Section section, boolean empty) {
                    super.updateItem(section, empty);
                    if (empty || section == null) {
                        setText(null);
                    } else {
                        setText(section.getName());
                    }
                }
            });
        }
        
        updateSectionRequirement();
    }
    
    private void updateSectionRequirement() {
        String enrollmentStatus = enrollmentStatusComboBox.getValue();
        boolean isRequired = "Enrolled".equals(enrollmentStatus);
        
        // Enable/disable section ComboBox based on enrollment status
        sectionComboBox.setDisable(!isRequired);
        
        // Clear section if enrollment status is not "Enrolled"
        if (!isRequired && sectionComboBox.getValue() != null) {
            sectionComboBox.setValue(null);
        }
    }
    
    @FXML
    private void handleSave() {
        // Clear previous errors and field styles
        errorLabel.setVisible(false);
        errorLabel.setText("");
        clearFieldStyles();
        
        boolean hasErrors = false;
        
        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            highlightField(nameField);
            hasErrors = true;
        }
        
        if (birthdatePicker.getValue() == null) {
            highlightField(birthdatePicker);
            hasErrors = true;
        }
        
        // Validate age
        int age = 0;
        if (birthdatePicker.getValue() != null) {
            age = calculateAge(birthdatePicker.getValue());
            // Update age field if it's empty or different
            if (ageField.getText().trim().isEmpty() || 
                (!ageField.getText().trim().isEmpty() && 
                 Integer.parseInt(ageField.getText().trim()) != age)) {
                ageField.setText(String.valueOf(age));
            }
        } else if (!ageField.getText().trim().isEmpty()) {
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException e) {
                highlightField(ageField);
                hasErrors = true;
            }
        }
        
        if (age < 16) {
            highlightField(ageField);
            if (birthdatePicker.getValue() != null) {
                highlightField(birthdatePicker);
            }
            hasErrors = true;
        }
        
        if (sexComboBox.getValue() == null || sexComboBox.getValue().isEmpty()) {
            highlightComboBox(sexComboBox);
            hasErrors = true;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            highlightField(addressField);
            hasErrors = true;
        }
        
        // Validate contact number (Philippine format)
        String contactNumber = contactNumberField.getText().trim();
        if (contactNumber.isEmpty()) {
            highlightField(contactNumberField);
            hasErrors = true;
        } else if (contactNumber.length() != 11) {
            highlightField(contactNumberField);
            hasErrors = true;
        } else if (!isValidPhilippineContactNumber(contactNumber)) {
            highlightField(contactNumberField);
            hasErrors = true;
        }
        
        // Validate parent/guardian information
        if (parentGuardianNameField.getText().trim().isEmpty()) {
            highlightField(parentGuardianNameField);
            hasErrors = true;
        }
        
        String parentContact = parentGuardianContactField.getText().trim();
        if (parentContact.isEmpty()) {
            highlightField(parentGuardianContactField);
            hasErrors = true;
        } else if (parentContact.length() != 11) {
            highlightField(parentGuardianContactField);
            hasErrors = true;
        } else if (!isValidPhilippineContactNumber(parentContact)) {
            highlightField(parentGuardianContactField);
            hasErrors = true;
        }
        
        // Validate: Student and parent contact numbers must be different
        if (!contactNumber.isEmpty() && !parentContact.isEmpty() && contactNumber.equals(parentContact)) {
            highlightField(contactNumberField);
            highlightField(parentGuardianContactField);
            hasErrors = true;
        }
        
        if (parentGuardianRelationshipComboBox.getValue() == null || parentGuardianRelationshipComboBox.getValue().isEmpty()) {
            highlightComboBox(parentGuardianRelationshipComboBox);
            hasErrors = true;
        }
        
        if (gradeLevelComboBox.getValue() == null) {
            highlightComboBox(gradeLevelComboBox);
            hasErrors = true;
        }
        
        if (strandComboBox.getValue() == null || strandComboBox.getValue().isEmpty()) {
            highlightComboBox(strandComboBox);
            hasErrors = true;
        }
        
        // Validate previous school (must not be all-capital abbreviations)
        String previousSchool = previousSchoolField.getText().trim();
        if (previousSchool.isEmpty()) {
            highlightField(previousSchoolField);
            hasErrors = true;
        } else if (isAllCapitalAbbreviation(previousSchool)) {
            highlightField(previousSchoolField);
            hasErrors = true;
        }
        
        // Validate GWA
        double gwa = 0.0;
        if (gwaField.getText().trim().isEmpty()) {
            highlightField(gwaField);
            hasErrors = true;
        } else {
            try {
                gwa = Double.parseDouble(gwaField.getText().trim());
                if (gwa < 75.0 || gwa > 100.0) {
                    highlightField(gwaField);
                    hasErrors = true;
                }
            } catch (NumberFormatException e) {
                highlightField(gwaField);
                hasErrors = true;
            }
        }
        
        // Validate LRN (must be exactly 12 digits)
        String lrn = lrnField.getText().trim();
        if (lrn.isEmpty()) {
            highlightField(lrnField);
            hasErrors = true;
        } else if (lrn.length() != 12) {
            highlightField(lrnField);
            hasErrors = true;
        } else if (!lrn.matches("^\\d{12}$")) {
            highlightField(lrnField);
            hasErrors = true;
        }
        
        if (enrollmentStatusComboBox.getValue() == null || enrollmentStatusComboBox.getValue().isEmpty()) {
            highlightComboBox(enrollmentStatusComboBox);
            hasErrors = true;
        }
        
        // Show error message if validation failed
        if (hasErrors) {
            StringBuilder errorMessage = new StringBuilder("Please correct the following errors:\n\n");
            
            if (nameField.getText().trim().isEmpty()) {
                errorMessage.append("• Name is required.\n");
            }
            if (birthdatePicker.getValue() == null) {
                errorMessage.append("• Birthdate is required.\n");
            }
            if (age < 16 && birthdatePicker.getValue() != null) {
                errorMessage.append("• Student age must be at least 16 years old.\n");
            }
            if (sexComboBox.getValue() == null || sexComboBox.getValue().isEmpty()) {
                errorMessage.append("• Sex is required.\n");
            }
            if (addressField.getText().trim().isEmpty()) {
                errorMessage.append("• Address is required.\n");
            }
            if (contactNumber.isEmpty()) {
                errorMessage.append("• Contact Number is required.\n");
            } else if (contactNumber.length() != 11) {
                errorMessage.append("• Contact Number must be exactly 11 digits.\n");
            } else if (!isValidPhilippineContactNumber(contactNumber)) {
                errorMessage.append("• Contact Number must follow Philippine format (09XXXXXXXXX - 11 digits starting with 09).\n");
            }
            if (parentGuardianNameField.getText().trim().isEmpty()) {
                errorMessage.append("• Parent/Guardian Name is required.\n");
            }
            if (parentContact.isEmpty()) {
                errorMessage.append("• Parent/Guardian Contact Number is required.\n");
            } else if (parentContact.length() != 11) {
                errorMessage.append("• Parent/Guardian Contact Number must be exactly 11 digits.\n");
            } else if (!isValidPhilippineContactNumber(parentContact)) {
                errorMessage.append("• Parent/Guardian Contact Number must follow Philippine format (09XXXXXXXXX - 11 digits starting with 09).\n");
            }
            if (!contactNumber.isEmpty() && !parentContact.isEmpty() && contactNumber.equals(parentContact)) {
                errorMessage.append("• Student contact number cannot be the same as parent/guardian contact number.\n");
            }
            if (parentGuardianRelationshipComboBox.getValue() == null || parentGuardianRelationshipComboBox.getValue().isEmpty()) {
                errorMessage.append("• Relationship is required.\n");
            }
            if (gradeLevelComboBox.getValue() == null) {
                errorMessage.append("• Grade Level is required.\n");
            }
            if (strandComboBox.getValue() == null || strandComboBox.getValue().isEmpty()) {
                errorMessage.append("• Strand is required.\n");
            }
            if (previousSchool.isEmpty()) {
                errorMessage.append("• Previous School is required.\n");
            } else if (isAllCapitalAbbreviation(previousSchool)) {
                errorMessage.append("• Previous School must be spelled out in full. Abbreviations like SNHS are not allowed.\n");
            }
            if (gwaField.getText().trim().isEmpty()) {
                errorMessage.append("• GWA is required.\n");
            } else {
                try {
                    double gwaCheck = Double.parseDouble(gwaField.getText().trim());
                    if (gwaCheck < 75.0 || gwaCheck > 100.0) {
                        errorMessage.append("• GWA must be between 75.00 and 100.00.\n");
                    }
                } catch (NumberFormatException e) {
                    errorMessage.append("• GWA must be a valid number.\n");
                }
            }
            if (lrn.isEmpty()) {
                errorMessage.append("• LRN is required.\n");
            } else if (lrn.length() != 12) {
                errorMessage.append("• LRN must be exactly 12 digits.\n");
            } else if (!lrn.matches("^\\d{12}$")) {
                errorMessage.append("• LRN must contain only numbers.\n");
            }
            if (enrollmentStatusComboBox.getValue() == null || enrollmentStatusComboBox.getValue().isEmpty()) {
                errorMessage.append("• Enrollment Status is required.\n");
            }
            
            showError(errorMessage.toString());
            return;
        }
        
        // Validate section assignment rules
        String enrollmentStatus = enrollmentStatusComboBox.getValue();
        if ("Enrolled".equals(enrollmentStatus) && sectionComboBox.getValue() == null) {
            showError("Enrolled students must be assigned to a section.");
            return;
        }
        
        // Validate: Non-enrolled students cannot have a section
        if (!"Enrolled".equals(enrollmentStatus) && sectionComboBox.getValue() != null) {
            showError("Sections can only be assigned to enrolled students. Please change the enrollment status to 'Enrolled' or remove the section assignment.");
            return;
        }
        
        // Disable save button and show loading state
        saveButton.setDisable(true);
        saveButton.setText("Saving...");
        
        // Create StudentDto with all validated data
        StudentDto studentDto = new StudentDto();
        studentDto.setName(nameField.getText().trim());
        studentDto.setBirthdate(birthdatePicker.getValue());
        studentDto.setAge(age);
        studentDto.setSex(sexComboBox.getValue());
        studentDto.setAddress(addressField.getText().trim());
        studentDto.setContactNumber(contactNumber);
        studentDto.setParentGuardianName(parentGuardianNameField.getText().trim());
        studentDto.setParentGuardianContact(parentContact);
        studentDto.setParentGuardianRelationship(parentGuardianRelationshipComboBox.getValue());
        studentDto.setGradeLevel(gradeLevelComboBox.getValue());
        studentDto.setStrand(strandComboBox.getValue());
        
        // Set section ID if section is selected (only for Enrolled students)
        if (sectionComboBox.getValue() != null && "Enrolled".equals(enrollmentStatus)) {
            studentDto.setSectionId(sectionComboBox.getValue().getId());
        } else {
            studentDto.setSectionId(null);
        }
        
        studentDto.setPreviousSchool(previousSchool);
        studentDto.setGwa(gwa);
        studentDto.setLrn(lrn);
        studentDto.setEnrollmentStatus(enrollmentStatus);
        
        // Save student in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Save student (this is a blocking database operation)
                StudentDto savedStudent = studentService.saveStudent(studentDto);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Save Student");
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Student Added Successfully");
                    alert.setContentText("Student " + savedStudent.getName() + " has been saved successfully.");
                    alert.showAndWait();
                    
                    // Close the form
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                // Update UI on JavaFX thread to show error
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Save Student");
                    showError("Error saving student: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private int calculateAge(LocalDate birthdate) {
        if (birthdate == null) {
            return 0;
        }
        return LocalDate.now().getYear() - birthdate.getYear() - 
               (LocalDate.now().getDayOfYear() < birthdate.getDayOfYear() ? 1 : 0);
    }
    
    private boolean isAllCapitalAbbreviation(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String trimmed = text.trim();
        
        // Exception: STI is allowed (it's a real school name)
        if (trimmed.equalsIgnoreCase("STI")) {
            return false;
        }
        
        // Check if text is all uppercase or all lowercase and contains only letters (no spaces or special chars)
        // Also check if it's short (likely an abbreviation)
        if (trimmed.length() <= 10 && (trimmed.matches("^[A-Z]+$") || trimmed.matches("^[a-z]+$"))) {
            return true;
        }
        
        // Check if text is all uppercase or all lowercase with spaces but still looks like abbreviation (e.g., "S N H S" or "s n h s")
        if ((trimmed.matches("^[A-Z\\s]+$") || trimmed.matches("^[a-z\\s]+$")) && trimmed.length() <= 15) {
            // Count words - if more than 3 words, probably not an abbreviation
            String[] words = trimmed.split("\\s+");
            if (words.length <= 3 && trimmed.length() <= 15) {
                // Check if average word length is very short (likely abbreviation)
                int totalLength = 0;
                for (String word : words) {
                    totalLength += word.length();
                }
                double avgLength = (double) totalLength / words.length;
                if (avgLength <= 3) {
                    return true;
                }
            }
        }
        
        // Check for patterns like "SNHS", "snhs", "S.N.H.S.", "S-N-H-S", "s.n.h.s"
        if (trimmed.matches("^[A-Za-z]{2,10}([.\\-\\s][A-Za-z]{1,5}){0,5}$")) {
            // Remove separators and check if it's all letters and short
            String withoutSeparators = trimmed.replaceAll("[.\\-\\s]", "");
            if (withoutSeparators.length() <= 10 && (withoutSeparators.matches("^[A-Z]+$") || withoutSeparators.matches("^[a-z]+$"))) {
                // Exception: STI is allowed (case-insensitive)
                if (!withoutSeparators.equalsIgnoreCase("STI")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
    }
    
    private void highlightField(DatePicker field) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
    }
    
    private void highlightComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
    }
    
    private void clearFieldStyles() {
        nameField.setStyle("");
        birthdatePicker.setStyle("");
        ageField.setStyle("");
        sexComboBox.setStyle("");
        addressField.setStyle("");
        contactNumberField.setStyle("");
        parentGuardianNameField.setStyle("");
        parentGuardianContactField.setStyle("");
        parentGuardianRelationshipComboBox.setStyle("");
        gradeLevelComboBox.setStyle("");
        strandComboBox.setStyle("");
        previousSchoolField.setStyle("");
        gwaField.setStyle("");
        lrnField.setStyle("");
        enrollmentStatusComboBox.setStyle("");
    }
}

