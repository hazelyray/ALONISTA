package com.enrollment.system.controller;

import com.enrollment.system.model.Section;
import com.enrollment.system.model.Strand;
import com.enrollment.system.service.SectionService;
import com.enrollment.system.service.StrandService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class SectionsManagementController implements Initializable {
    
    // Strands Table
    @FXML
    private TableView<Strand> strandsTable;
    
    @FXML
    private TableColumn<Strand, Long> strandIdColumn;
    
    @FXML
    private TableColumn<Strand, String> strandNameColumn;
    
    @FXML
    private TableColumn<Strand, String> strandDescriptionColumn;
    
    @FXML
    private TableColumn<Strand, String> strandActionsColumn;
    
    // Sections Table
    @FXML
    private TableView<Section> sectionsTable;
    
    @FXML
    private TableColumn<Section, Long> sectionIdColumn;
    
    @FXML
    private TableColumn<Section, String> sectionNameColumn;
    
    @FXML
    private TableColumn<Section, String> sectionStrandColumn;
    
    @FXML
    private TableColumn<Section, Integer> sectionGradeLevelColumn;
    
    @FXML
    private TableColumn<Section, Integer> sectionCapacityColumn;
    
    @FXML
    private TableColumn<Section, String> sectionActionsColumn;
    
    // Buttons
    @FXML
    private Button addStrandButton;
    
    @FXML
    private Button addSectionButton;
    
    @FXML
    private Button refreshButton;
    
    // Labels
    @FXML
    private Label strandCountLabel;
    
    @FXML
    private Label sectionCountLabel;
    
    @Autowired
    private SectionService sectionService;
    
    @Autowired
    private StrandService strandService;
    
    private ObservableList<Strand> strandList;
    private FilteredList<Strand> filteredStrandList;
    
    private ObservableList<Section> sectionList;
    private FilteredList<Section> filteredSectionList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStrandsTable();
        setupSectionsTable();
        
        Platform.runLater(() -> {
            loadStrands();
            loadSections();
        });
    }
    
    private void setupStrandsTable() {
        strandIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        strandNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        strandDescriptionColumn.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            return new javafx.beans.property.SimpleStringProperty(desc != null ? desc : "");
        });
        
        strandActionsColumn.setCellFactory(column -> new TableCell<Strand, String>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                
                editButton.setOnAction(e -> {
                    Strand strand = getTableView().getItems().get(getIndex());
                    handleEditStrand(strand);
                });
                
                deleteButton.setOnAction(e -> {
                    Strand strand = getTableView().getItems().get(getIndex());
                    handleDeleteStrand(strand);
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
    }
    
    private void setupSectionsTable() {
        sectionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sectionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        sectionStrandColumn.setCellValueFactory(new PropertyValueFactory<>("strand"));
        sectionGradeLevelColumn.setCellValueFactory(new PropertyValueFactory<>("gradeLevel"));
        sectionCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        
        sectionActionsColumn.setCellFactory(column -> new TableCell<Section, String>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                
                editButton.setOnAction(e -> {
                    Section section = getTableView().getItems().get(getIndex());
                    handleEditSection(section);
                });
                
                deleteButton.setOnAction(e -> {
                    Section section = getTableView().getItems().get(getIndex());
                    handleDeleteSection(section);
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
    }
    
    public void refreshData() {
        loadStrands();
        loadSections();
    }
    
    private void loadStrands() {
        Platform.runLater(() -> {
            try {
                java.util.List<Strand> strands = strandService.getActiveStrands();
                strandList = FXCollections.observableArrayList(strands);
                
                filteredStrandList = new FilteredList<>(strandList, p -> true);
                SortedList<Strand> sortedList = new SortedList<>(filteredStrandList);
                sortedList.comparatorProperty().bind(strandsTable.comparatorProperty());
                
                strandsTable.setItems(sortedList);
                updateStrandCount();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading strands: " + e.getMessage());
            }
        });
    }
    
    private void loadSections() {
        Platform.runLater(() -> {
            try {
                java.util.List<Section> sections = sectionService.getActiveSections();
                sectionList = FXCollections.observableArrayList(sections);
                
                filteredSectionList = new FilteredList<>(sectionList, p -> true);
                SortedList<Section> sortedList = new SortedList<>(filteredSectionList);
                sortedList.comparatorProperty().bind(sectionsTable.comparatorProperty());
                
                sectionsTable.setItems(sortedList);
                updateSectionCount();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading sections: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadStrands();
        loadSections();
    }
    
    @FXML
    private void handleAddStrand() {
        showStrandDialog(null);
    }
    
    private void handleEditStrand(Strand strand) {
        showStrandDialog(strand);
    }
    
    private void handleDeleteStrand(Strand strand) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Strand");
        confirmAlert.setHeaderText("Delete Strand: " + strand.getName());
        confirmAlert.setContentText("Are you sure you want to delete this strand? This will set it as inactive.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    strandService.deleteStrand(strand.getId());
                    showSuccess("Strand " + strand.getName() + " has been deleted successfully.");
                    refreshData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error deleting strand: " + e.getMessage());
                }
            }
        });
    }
    
    private void showStrandDialog(Strand strand) {
        Dialog<Strand> dialog = new Dialog<>();
        dialog.setTitle(strand == null ? "Add Strand" : "Edit Strand");
        dialog.setHeaderText(strand == null ? "Add New Strand" : "Edit Strand: " + strand.getName());
        
        TextField nameField = new TextField();
        nameField.setPromptText("Strand Name (e.g., ABM, HUMSS, STEM)");
        
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description (optional)");
        
        if (strand != null) {
            nameField.setText(strand.getName());
            if (strand.getDescription() != null) {
                descriptionField.setText(strand.getDescription());
            }
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Strand Name *:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Strand result = strand != null ? strand : new Strand();
                result.setName(nameField.getText().trim());
                result.setDescription(descriptionField.getText().trim().isEmpty() ? null : descriptionField.getText().trim());
                if (strand == null) {
                    result.setIsActive(true);
                }
                return result;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                if (strand == null) {
                    strandService.createStrand(result.getName(), result.getDescription());
                    showSuccess("Strand " + result.getName() + " has been created successfully.");
                } else {
                    strandService.updateStrand(strand.getId(), result.getName(), result.getDescription());
                    showSuccess("Strand " + result.getName() + " has been updated successfully.");
                }
                refreshData();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error saving strand: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleAddSection() {
        showSectionDialog(null);
    }
    
    private void handleEditSection(Section section) {
        showSectionDialog(section);
    }
    
    private void handleDeleteSection(Section section) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Section");
        confirmAlert.setHeaderText("Delete Section: " + section.getName());
        confirmAlert.setContentText("Are you sure you want to delete this section? This will set it as inactive.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sectionService.deleteSection(section.getId());
                    showSuccess("Section " + section.getName() + " has been deleted successfully.");
                    refreshData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error deleting section: " + e.getMessage());
                }
            }
        });
    }
    
    private void showSectionDialog(Section section) {
        Dialog<Section> dialog = new Dialog<>();
        dialog.setTitle(section == null ? "Add Section" : "Edit Section");
        dialog.setHeaderText(section == null ? "Add New Section" : "Edit Section: " + section.getName());
        
        TextField nameField = new TextField();
        nameField.setPromptText("Section Name (e.g., ABM-11A)");
        
        ComboBox<String> strandComboBox = new ComboBox<>();
        try {
            java.util.List<Strand> strands = strandService.getActiveStrands();
            for (Strand strand : strands) {
                strandComboBox.getItems().add(strand.getName());
            }
        } catch (Exception e) {
            showError("Error loading strands: " + e.getMessage());
        }
        strandComboBox.setPromptText("Select Strand");
        
        ComboBox<Integer> gradeLevelComboBox = new ComboBox<>();
        gradeLevelComboBox.getItems().addAll(11, 12);
        gradeLevelComboBox.setPromptText("Select Grade Level");
        
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity (optional)");
        
        if (section != null) {
            nameField.setText(section.getName());
            strandComboBox.setValue(section.getStrand());
            gradeLevelComboBox.setValue(section.getGradeLevel());
            if (section.getCapacity() != null) {
                capacityField.setText(String.valueOf(section.getCapacity()));
            }
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Section Name *:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Strand *:"), 0, 1);
        grid.add(strandComboBox, 1, 1);
        grid.add(new Label("Grade Level *:"), 0, 2);
        grid.add(gradeLevelComboBox, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3);
        grid.add(capacityField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || 
                    strandComboBox.getValue() == null || 
                    gradeLevelComboBox.getValue() == null);
        });
        
        strandComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || 
                    newValue == null || 
                    gradeLevelComboBox.getValue() == null);
        });
        
        gradeLevelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || 
                    strandComboBox.getValue() == null || 
                    newValue == null);
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Section result = section != null ? section : new Section();
                result.setName(nameField.getText().trim());
                result.setStrand(strandComboBox.getValue());
                result.setGradeLevel(gradeLevelComboBox.getValue());
                
                if (!capacityField.getText().trim().isEmpty()) {
                    try {
                        result.setCapacity(Integer.parseInt(capacityField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Invalid capacity, leave as null
                    }
                }
                
                if (section == null) {
                    result.setIsActive(true);
                }
                
                return result;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                if (section == null) {
                    sectionService.createSection(result.getName(), result.getStrand(), result.getGradeLevel(), result.getCapacity());
                    showSuccess("Section " + result.getName() + " has been created successfully.");
                } else {
                    sectionService.updateSection(section.getId(), result.getName(), 
                        result.getStrand(), result.getGradeLevel(), result.getCapacity());
                    showSuccess("Section " + result.getName() + " has been updated successfully.");
                }
                refreshData();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error saving section: " + e.getMessage());
            }
        });
    }
    
    private void updateStrandCount() {
        if (filteredStrandList != null && strandCountLabel != null) {
            int count = filteredStrandList.size();
            strandCountLabel.setText("Total Strands: " + count);
        }
    }
    
    private void updateSectionCount() {
        if (filteredSectionList != null && sectionCountLabel != null) {
            int count = filteredSectionList.size();
            sectionCountLabel.setText("Total Sections: " + count);
        }
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
