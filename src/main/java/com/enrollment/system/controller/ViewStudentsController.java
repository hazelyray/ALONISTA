package com.enrollment.system.controller;

import com.enrollment.system.dto.StudentDto;
import com.enrollment.system.service.StudentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ViewStudentsController implements Initializable {
    
    @FXML
    private TableView<StudentDto> studentsTable;
    
    @FXML
    private TableColumn<StudentDto, Long> idColumn;
    
    @FXML
    private TableColumn<StudentDto, String> rowNumberColumn;
    
    @FXML
    private TableColumn<StudentDto, String> nameColumn;
    
    @FXML
    private TableColumn<StudentDto, Integer> ageColumn;
    
    @FXML
    private TableColumn<StudentDto, String> sexColumn;
    
    @FXML
    private TableColumn<StudentDto, Integer> gradeLevelColumn;
    
    @FXML
    private TableColumn<StudentDto, String> strandColumn;
    
    @FXML
    private TableColumn<StudentDto, String> sectionColumn;
    
    @FXML
    private TableColumn<StudentDto, String> lrnColumn;
    
    @FXML
    private TableColumn<StudentDto, String> contactColumn;
    
    @FXML
    private TableColumn<StudentDto, String> parentGuardianColumn;
    
    @FXML
    private TableColumn<StudentDto, String> enrollmentStatusColumn;
    
    @FXML
    private TableColumn<StudentDto, String> actionsColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<Integer> gradeFilterComboBox;
    
    @FXML
    private ComboBox<String> strandFilterComboBox;
    
    @FXML
    private ComboBox<String> enrollmentStatusFilterComboBox;
    
    @FXML
    private Label studentCountLabel;
    
    
    @FXML
    private Button refreshButton;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private ObservableList<StudentDto> studentList;
    private FilteredList<StudentDto> filteredList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize filter comboboxes
        gradeFilterComboBox.getItems().addAll(null, 11, 12);
        strandFilterComboBox.getItems().addAll(null, "ABM", "HUMSS", "STEM", "GAS", "TVL");
        enrollmentStatusFilterComboBox.getItems().addAll(null, "Enrolled", "Pending");
        
        // Initialize table columns with null-safe cell value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Row number column - displays sequential numbers (1, 2, 3, etc.)
        rowNumberColumn.setCellFactory(column -> new TableCell<StudentDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    // Get the index from the table's items (filtered/sorted list)
                    int index = getIndex();
                    setText(String.valueOf(index + 1));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        nameColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getName();
            return new SimpleStringProperty(name != null ? name : "");
        });
        
        ageColumn.setCellValueFactory(cellData -> {
            Integer age = cellData.getValue().getAge();
            return new SimpleIntegerProperty(age != null ? age : 0).asObject();
        });
        
        sexColumn.setCellValueFactory(cellData -> {
            String sex = cellData.getValue().getSex();
            return new SimpleStringProperty(sex != null ? sex : "");
        });
        
        gradeLevelColumn.setCellValueFactory(cellData -> {
            Integer grade = cellData.getValue().getGradeLevel();
            return new SimpleIntegerProperty(grade != null ? grade : 0).asObject();
        });
        
        strandColumn.setCellValueFactory(cellData -> {
            String strand = cellData.getValue().getStrand();
            return new SimpleStringProperty(strand != null ? strand : "N/A");
        });
        
        sectionColumn.setCellValueFactory(cellData -> {
            String sectionName = cellData.getValue().getSectionName();
            return new SimpleStringProperty(sectionName != null ? sectionName : "N/A");
        });
        
        lrnColumn.setCellValueFactory(cellData -> {
            String lrn = cellData.getValue().getLrn();
            return new SimpleStringProperty(lrn != null ? lrn : "");
        });
        
        contactColumn.setCellValueFactory(cellData -> {
            String contact = cellData.getValue().getContactNumber();
            return new SimpleStringProperty(contact != null ? contact : "");
        });
        
        enrollmentStatusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getEnrollmentStatus();
            return new SimpleStringProperty(status != null ? status : "");
        });
        
        // Parent/Guardian column - custom cell value factory
        parentGuardianColumn.setCellValueFactory(cellData -> {
            StudentDto student = cellData.getValue();
            String parentInfo = student.getParentGuardianName();
            if (parentInfo == null || parentInfo.isEmpty()) {
                return new SimpleStringProperty("N/A");
            }
            return new SimpleStringProperty(parentInfo);
        });
        
        // Actions column - custom cell factory for buttons
        actionsColumn.setCellFactory(column -> new TableCell<StudentDto, String>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                
                editButton.setOnAction(e -> {
                    StudentDto student = getTableView().getItems().get(getIndex());
                    handleEdit(student);
                });
                
                deleteButton.setOnAction(e -> {
                    StudentDto student = getTableView().getItems().get(getIndex());
                    handleDelete(student);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hbox.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });
        
        // Ensure table is properly initialized
        if (studentsTable != null) {
            studentsTable.setPlaceholder(new Label("Loading students..."));
        }
        
        // Load students after a short delay to ensure UI is ready
        Platform.runLater(() -> {
            loadStudents();
        });
    }
    
    // Public method to refresh data (can be called from outside)
    public void refreshData() {
        loadStudents();
    }
    
    private void loadStudents() {
        Platform.runLater(() -> {
            try {
                // Load students from service
                java.util.List<StudentDto> students = studentService.getAllStudents();
                
                // Create observable list
                studentList = FXCollections.observableArrayList(students);
                
                // Create filtered list
                filteredList = new FilteredList<>(studentList, p -> true);
                
                // Create sorted list
                SortedList<StudentDto> sortedList = new SortedList<>(filteredList);
                sortedList.comparatorProperty().bind(studentsTable.comparatorProperty());
                
                // Set table items
                studentsTable.setItems(sortedList);
                
                // Update count
                updateStudentCount();
                
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading students: " + e.getMessage());
                // Set empty list on error
                studentList = FXCollections.observableArrayList();
                filteredList = new FilteredList<>(studentList, p -> true);
                studentsTable.setItems(new SortedList<>(filteredList));
                updateStudentCount();
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadStudents();
        searchField.clear();
        gradeFilterComboBox.setValue(null);
        strandFilterComboBox.setValue(null);
        enrollmentStatusFilterComboBox.setValue(null);
    }
    
    @FXML
    private void handleSearch() {
        applyFilters();
    }
    
    @FXML
    private void handleFilter() {
        applyFilters();
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        Integer gradeFilter = gradeFilterComboBox.getValue();
        String strandFilter = strandFilterComboBox.getValue();
        String enrollmentStatusFilter = enrollmentStatusFilterComboBox.getValue();
        
        filteredList.setPredicate(student -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    (student.getName() != null && student.getName().toLowerCase().contains(searchText)) ||
                    (student.getLrn() != null && student.getLrn().toLowerCase().contains(searchText)) ||
                    (student.getContactNumber() != null && student.getContactNumber().contains(searchText));
            
            // Grade filter
            boolean matchesGrade = gradeFilter == null || 
                    (student.getGradeLevel() != null && student.getGradeLevel().equals(gradeFilter));
            
            // Strand filter
            boolean matchesStrand = strandFilter == null || 
                    (student.getStrand() != null && student.getStrand().equals(strandFilter));
            
            // Enrollment Status filter
            boolean matchesEnrollmentStatus = enrollmentStatusFilter == null || 
                    (student.getEnrollmentStatus() != null && student.getEnrollmentStatus().equals(enrollmentStatusFilter));
            
            return matchesSearch && matchesGrade && matchesStrand && matchesEnrollmentStatus;
        });
        
        updateStudentCount();
    }
    
    private void updateStudentCount() {
        if (filteredList != null && studentCountLabel != null) {
            int count = filteredList.size();
            studentCountLabel.setText("Total Students: " + count);
        }
    }
    
    private void handleEdit(StudentDto student) {
        try {
            // Load Edit Student FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/EditStudent.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent editStudentRoot = loader.load();
            
            // Get the controller and set student data
            EditStudentController controller = loader.getController();
            controller.setStudent(student);
            
            // Create new stage for Edit Student form
            Stage editStudentStage = new Stage();
            editStudentStage.setTitle("Edit Student - Seguinon SHS Enrollment System");
            editStudentStage.setScene(new Scene(editStudentRoot));
            editStudentStage.setWidth(1200);
            editStudentStage.setHeight(750);
            editStudentStage.setResizable(false);
            
            // Set owner to View Students stage
            Stage viewStudentsStage = (Stage) studentsTable.getScene().getWindow();
            editStudentStage.initOwner(viewStudentsStage);
            
            // Refresh data when window is closed
            editStudentStage.setOnHidden(e -> refreshData());
            
            // Show Edit Student form
            editStudentStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading edit form: " + e.getMessage());
        }
    }
    
    private void handleDelete(StudentDto student) {
        // Create custom dialog for archive reason
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Archive Student");
        dialog.setHeaderText("Archive Student: " + student.getName());
        dialog.setContentText("Please select the reason for archiving:");
        
        // Create ComboBox for archive reasons
        ComboBox<String> reasonComboBox = new ComboBox<>();
        reasonComboBox.getItems().addAll("DROPPED OUT", "GRADUATED", "TRANSFERRED TO ANOTHER SCHOOL");
        reasonComboBox.setPrefWidth(300);
        reasonComboBox.setPromptText("Select reason...");
        
        // Set button types
        ButtonType archiveButtonType = new ButtonType("Archive", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(archiveButtonType, ButtonType.CANCEL);
        
        // Add ComboBox to dialog
        dialog.getDialogPane().setContent(reasonComboBox);
        
        // Enable/disable archive button based on selection
        dialog.getDialogPane().lookupButton(archiveButtonType).setDisable(true);
        reasonComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            dialog.getDialogPane().lookupButton(archiveButtonType).setDisable(newValue == null);
        });
        
        // Convert result when archive button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == archiveButtonType) {
                return reasonComboBox.getValue();
            }
            return null;
        });
        
        // Show dialog and process result
        dialog.showAndWait().ifPresent(reason -> {
            if (reason != null && !reason.isEmpty()) {
                // Archive student in background thread to avoid blocking UI
                new Thread(() -> {
                    try {
                        // Archive student (this is a blocking database operation)
                        studentService.archiveStudent(student.getId(), reason);
                        
                        // Update UI on JavaFX thread
                        Platform.runLater(() -> {
                            // Show success message
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText("Student Archived");
                            successAlert.setContentText("Student " + student.getName() + " has been archived successfully.\nReason: " + reason);
                            successAlert.showAndWait();
                            
                            // Refresh the table
                            refreshData();
                        });
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Update UI on JavaFX thread to show error
                        Platform.runLater(() -> {
                            showError("Error archiving student: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

