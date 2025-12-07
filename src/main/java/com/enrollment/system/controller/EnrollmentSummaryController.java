package com.enrollment.system.controller;

import com.enrollment.system.service.ReportService;
import com.enrollment.system.service.ReportService.EnrollmentStatistics;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ResourceBundle;

@Component
public class EnrollmentSummaryController implements Initializable {
    
    @FXML
    private VBox mainContainer;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private Button exportPdfButton;
    
    @FXML
    private Button exportExcelButton;
    
    @Autowired
    private ReportService reportService;
    
    private EnrollmentStatistics statistics;
    private VBox contentContainer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));
        scrollPane.setContent(contentContainer);
        scrollPane.setFitToWidth(true);
        
        loadData();
    }
    
    private void loadData() {
        new Thread(() -> {
            try {
                statistics = reportService.getEnrollmentStatistics();
                
                Platform.runLater(() -> {
                    buildSummaryView();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to load data");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void buildSummaryView() {
        contentContainer.getChildren().clear();
        
        // Title
        Label titleLabel = new Label("📊 Enrollment Summary / Statistics");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contentContainer.getChildren().add(titleLabel);
        
        // Summary Cards
        HBox summaryCards = new HBox(20);
        summaryCards.setAlignment(Pos.CENTER_LEFT);
        summaryCards.getChildren().addAll(
            createStatCard("Total Enrolled", String.valueOf(statistics.totalEnrolled), "#3498db"),
            createStatCard("Pending Applications", String.valueOf(statistics.totalPending), "#f39c12")
        );
        contentContainer.getChildren().add(summaryCards);
        
        // Charts Row 1: Grade Level and Strand
        HBox chartsRow1 = new HBox(20);
        chartsRow1.setAlignment(Pos.CENTER_LEFT);
        
        // Grade Level Bar Chart
        BarChart<String, Number> gradeLevelChart = createGradeLevelChart();
        gradeLevelChart.setPrefWidth(500);
        gradeLevelChart.setPrefHeight(400);
        
        // Strand Bar Chart
        BarChart<String, Number> strandChart = createStrandChart();
        strandChart.setPrefWidth(500);
        strandChart.setPrefHeight(400);
        
        chartsRow1.getChildren().addAll(gradeLevelChart, strandChart);
        contentContainer.getChildren().add(chartsRow1);
        
        // Charts Row 2: Gender Pie Chart and Section Chart
        HBox chartsRow2 = new HBox(20);
        chartsRow2.setAlignment(Pos.CENTER_LEFT);
        
        // Gender Pie Chart
        PieChart genderChart = createGenderChart();
        genderChart.setPrefWidth(500);
        genderChart.setPrefHeight(400);
        
        // Section Bar Chart
        BarChart<String, Number> sectionChart = createSectionChart();
        sectionChart.setPrefWidth(500);
        sectionChart.setPrefHeight(400);
        
        chartsRow2.getChildren().addAll(genderChart, sectionChart);
        contentContainer.getChildren().add(chartsRow2);
        
        // Statistics Tables
        VBox tablesContainer = new VBox(20);
        
        // Grade Level Table
        if (statistics.byGradeLevel != null && !statistics.byGradeLevel.isEmpty()) {
            Label gradeLabel = new Label("Students by Grade Level");
            gradeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            tablesContainer.getChildren().add(gradeLabel);
            tablesContainer.getChildren().add(createStatisticsTable("Grade Level", "Count", statistics.byGradeLevel));
        }
        
        // Strand Table
        if (statistics.byStrand != null && !statistics.byStrand.isEmpty()) {
            Label strandLabel = new Label("Students by Strand");
            strandLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            tablesContainer.getChildren().add(strandLabel);
            tablesContainer.getChildren().add(createStatisticsTable("Strand", "Count", statistics.byStrand));
        }
        
        contentContainer.getChildren().add(tablesContainer);
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefWidth(250);
        card.setPrefHeight(150);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 48px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + color + ";"
        );
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private BarChart<String, Number> createGradeLevelChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Students by Grade Level");
        chart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (statistics.byGradeLevel != null) {
            statistics.byGradeLevel.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> series.getData().add(new XYChart.Data<>("Grade " + entry.getKey(), entry.getValue())));
        }
        chart.getData().add(series);
        
        return chart;
    }
    
    private BarChart<String, Number> createStrandChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Students by Strand");
        chart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (statistics.byStrand != null) {
            statistics.byStrand.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
        }
        chart.getData().add(series);
        
        return chart;
    }
    
    private PieChart createGenderChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Students by Gender");
        
        if (statistics.byGender != null) {
            statistics.byGender.forEach((gender, count) -> {
                PieChart.Data slice = new PieChart.Data(gender + " (" + count + ")", count);
                chart.getData().add(slice);
            });
        }
        
        return chart;
    }
    
    private BarChart<String, Number> createSectionChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Students by Section");
        chart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (statistics.bySection != null) {
            statistics.bySection.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10) // Top 10 sections
                    .forEach(entry -> {
                        String shortName = entry.getKey();
                        if (shortName.length() > 20) {
                            shortName = shortName.substring(0, 17) + "...";
                        }
                        series.getData().add(new XYChart.Data<>(shortName, entry.getValue()));
                    });
        }
        chart.getData().add(series);
        
        return chart;
    }
    
    private TableView<Map<String, String>> createStatisticsTable(String col1Header, String col2Header, Map<?, Long> data) {
        TableView<Map<String, String>> table = new TableView<>();
        
        TableColumn<Map<String, String>, String> col1 = new TableColumn<>(col1Header);
        col1.setCellValueFactory(rowData -> {
            Map<String, String> row = rowData.getValue();
            return new javafx.beans.property.SimpleStringProperty(row.get("key"));
        });
        col1.setPrefWidth(300);
        
        TableColumn<Map<String, String>, String> col2 = new TableColumn<>(col2Header);
        col2.setCellValueFactory(rowData -> {
            Map<String, String> row = rowData.getValue();
            return new javafx.beans.property.SimpleStringProperty(row.get("value"));
        });
        col2.setPrefWidth(150);
        
        @SuppressWarnings("unchecked")
        TableColumn<Map<String, String>, String>[] columns = new TableColumn[] {col1, col2};
        table.getColumns().addAll(columns);
        
        javafx.collections.ObservableList<Map<String, String>> tableData = javafx.collections.FXCollections.observableArrayList();
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(java.util.Comparator.reverseOrder()))
                .forEach(entry -> {
                    Map<String, String> row = new HashMap<>();
                    row.put("key", entry.getKey().toString());
                    row.put("value", String.valueOf(entry.getValue()));
                    tableData.add(row);
                });
        
        table.setItems(tableData);
        table.setPrefHeight(200);
        table.setStyle("-fx-background-color: white;");
        
        return table;
    }
    
    @FXML
    private void exportToPDF() {
        if (statistics == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Data");
            alert.setHeaderText("No statistics available");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Enrollment_Summary_Report.pdf");
        
        Stage stage = (Stage) exportPdfButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            new Thread(() -> {
                try {
                    generatePDF(file);
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
    
    @FXML
    private void exportToExcel() {
        if (statistics == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Data");
            alert.setHeaderText("No statistics available");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("Enrollment_Summary_Report.xlsx");
        
        Stage stage = (Stage) exportExcelButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            new Thread(() -> {
                try {
                    generateExcel(file);
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
    
    private void generatePDF(File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = 750;
                
                // Use default fonts - PDFBox 3.0.1 uses different API
                PDType1Font fontBold = new PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font font = new PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);
                
                // Title
                contentStream.beginText();
                contentStream.setFont(fontBold, 16);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("ENROLLMENT SUMMARY / STATISTICS");
                contentStream.endText();
                
                yPosition -= 25;
                
                // Date
                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
                contentStream.endText();
                
                yPosition -= 30;
                
                // Summary
                contentStream.beginText();
                contentStream.setFont(fontBold, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Summary");
                contentStream.endText();
                
                yPosition -= 20;
                contentStream.setFont(font, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Total Enrolled: " + statistics.totalEnrolled);
                contentStream.endText();
                
                yPosition -= 15;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Pending Applications: " + statistics.totalPending);
                contentStream.endText();
                
                yPosition -= 30;
                
                // By Grade Level
                if (statistics.byGradeLevel != null && !statistics.byGradeLevel.isEmpty()) {
                    contentStream.setFont(fontBold, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Students by Grade Level");
                    contentStream.endText();
                    
                    yPosition -= 20;
                    contentStream.setFont(font, 10);
                    for (Map.Entry<Integer, Long> entry : statistics.byGradeLevel.entrySet()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 20, yPosition);
                        contentStream.showText("Grade " + entry.getKey() + ": " + entry.getValue());
                        contentStream.endText();
                        yPosition -= 15;
                    }
                    yPosition -= 10;
                }
                
                // By Strand
                if (statistics.byStrand != null && !statistics.byStrand.isEmpty()) {
                    contentStream.setFont(fontBold, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Students by Strand");
                    contentStream.endText();
                    
                    yPosition -= 20;
                    contentStream.setFont(font, 10);
                    for (Map.Entry<String, Long> entry : statistics.byStrand.entrySet()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 20, yPosition);
                        contentStream.showText(entry.getKey() + ": " + entry.getValue());
                        contentStream.endText();
                        yPosition -= 15;
                    }
                    yPosition -= 10;
                }
                
                // By Gender
                if (statistics.byGender != null && !statistics.byGender.isEmpty()) {
                    contentStream.setFont(fontBold, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Students by Gender");
                    contentStream.endText();
                    
                    yPosition -= 20;
                    contentStream.setFont(font, 10);
                    for (Map.Entry<String, Long> entry : statistics.byGender.entrySet()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 20, yPosition);
                        contentStream.showText(entry.getKey() + ": " + entry.getValue());
                        contentStream.endText();
                        yPosition -= 15;
                    }
                }
            }
            
            document.save(file);
        }
    }
    
    private void generateExcel(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enrollment Summary");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("ENROLLMENT SUMMARY / STATISTICS");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Date
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            
            rowNum++; // Empty row
            
            // Summary
            Row summaryHeader = sheet.createRow(rowNum++);
            summaryHeader.createCell(0).setCellValue("Summary");
            summaryHeader.getCell(0).setCellStyle(headerStyle);
            
            Row enrolledRow = sheet.createRow(rowNum++);
            enrolledRow.createCell(0).setCellValue("Total Enrolled");
            enrolledRow.createCell(1).setCellValue(statistics.totalEnrolled);
            
            Row pendingRow = sheet.createRow(rowNum++);
            pendingRow.createCell(0).setCellValue("Pending Applications");
            pendingRow.createCell(1).setCellValue(statistics.totalPending);
            
            rowNum++; // Empty row
            
            // By Grade Level
            if (statistics.byGradeLevel != null && !statistics.byGradeLevel.isEmpty()) {
                Row gradeHeader = sheet.createRow(rowNum++);
                gradeHeader.createCell(0).setCellValue("Students by Grade Level");
                gradeHeader.getCell(0).setCellStyle(headerStyle);
                
                for (Map.Entry<Integer, Long> entry : statistics.byGradeLevel.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Grade " + entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                }
                rowNum++; // Empty row
            }
            
            // By Strand
            if (statistics.byStrand != null && !statistics.byStrand.isEmpty()) {
                Row strandHeader = sheet.createRow(rowNum++);
                strandHeader.createCell(0).setCellValue("Students by Strand");
                strandHeader.getCell(0).setCellStyle(headerStyle);
                
                for (Map.Entry<String, Long> entry : statistics.byStrand.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                }
                rowNum++; // Empty row
            }
            
            // By Gender
            if (statistics.byGender != null && !statistics.byGender.isEmpty()) {
                Row genderHeader = sheet.createRow(rowNum++);
                genderHeader.createCell(0).setCellValue("Students by Gender");
                genderHeader.getCell(0).setCellStyle(headerStyle);
                
                for (Map.Entry<String, Long> entry : statistics.byGender.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                }
            }
            
            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }
}

