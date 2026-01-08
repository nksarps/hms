package com.nks.hms.ui;

import com.nks.hms.controller.PrescriptionController;
import com.nks.hms.model.Prescription;
import com.nks.hms.model.Patient;
import com.nks.hms.model.Doctor;
import com.nks.hms.db.Database;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder for prescription management tab.
 * 
 * <p>Separates UI construction from Main class (Single Responsibility Principle).
 * Encapsulates all prescription tab layout and event wiring logic.
 */
public class PrescriptionTabBuilder {
    private final PrescriptionController controller;
    
    public PrescriptionTabBuilder(PrescriptionController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete prescription management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Prescriptions");
        tab.setClosable(false);

        // Configure main table
        TableView<Prescription> table = buildPrescriptionTable();

        // Search and sort controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search by patient or doctor name");
        Button searchBtn = new Button("Search");
        ComboBox<String> sortCombo = buildSortComboBox();
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn, 
                new Label("Sort:"), sortCombo);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

        // Form fields
        GridPane form = buildForm();
        ComboBox<Integer> patientCombo = (ComboBox<Integer>) form.lookup("#patientCombo");
        ComboBox<Integer> doctorCombo = (ComboBox<Integer>) form.lookup("#doctorCombo");
        DatePicker prescriptionDatePicker = (DatePicker) form.lookup("#prescriptionDatePicker");
        TextArea notesArea = (TextArea) form.lookup("#notesArea");
        @SuppressWarnings("unchecked")
        TableView<String> itemsTable = (TableView<String>) form.lookup("#itemsTable");

        // Load patients and doctors into combo boxes
        loadPatients(patientCombo);
        loadDoctors(doctorCombo);

        // Load initial prescription data into table
        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);
        controller.loadPrescriptions(table, pagination, "", sortCombo.getValue(), feedback);

        Button saveBtn = new Button("Save / Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        HBox actionButtons = new HBox(10, saveBtn, deleteBtn, clearBtn);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        VBox rightPane = new VBox(12, form, actionButtons, feedback);
        rightPane.setPadding(new Insets(10));
        rightPane.setPrefWidth(450);

        VBox leftPane = new VBox(10, searchBox, table, pagination);
        VBox.setVgrow(table, Priority.ALWAYS);
        leftPane.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setCenter(leftPane);
        layout.setRight(rightPane);
        tab.setContent(layout);

        // Wire up event handlers
        wireEventHandlers(table, pagination, searchField, searchBtn, sortCombo, 
                         feedback, saveBtn, deleteBtn, clearBtn, patientCombo, doctorCombo,
                         prescriptionDatePicker, notesArea, itemsTable);
        
        return tab;
    }
    
    private TableView<Prescription> buildPrescriptionTable() {
        TableView<Prescription> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Prescription, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Prescription, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        
        TableColumn<Prescription, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        
        TableColumn<Prescription, LocalDate> dateCol = new TableColumn<>("Prescription Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("prescriptionDate"));
        
        TableColumn<Prescription, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        @SuppressWarnings("unchecked")
        var columns = new TableColumn[] {idCol, patientCol, doctorCol, dateCol, notesCol};
        table.getColumns().addAll(columns);
        return table;
    }
    
    private ComboBox<String> buildSortComboBox() {
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
                "All", "Date (Oldest)", "Date (Newest)", 
                "Patient (A-Z)", "Patient (Z-A)", "Doctor (A-Z)", "Doctor (Z-A)"));
        sortCombo.setValue("All");
        sortCombo.setPrefWidth(150);
        return sortCombo;
    }
    
    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        
        // Set column constraints to prevent label truncation
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(150);
        col1.setPrefWidth(150);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setPrefWidth(250);
        form.getColumnConstraints().addAll(col1, col2);

        ComboBox<Integer> patientCombo = new ComboBox<>();
        patientCombo.setPromptText("Select Patient");
        patientCombo.setPrefWidth(200);
        patientCombo.setId("patientCombo");
        
        ComboBox<Integer> doctorCombo = new ComboBox<>();
        doctorCombo.setPromptText("Select Doctor");
        doctorCombo.setPrefWidth(200);
        doctorCombo.setId("doctorCombo");
        
        DatePicker prescriptionDatePicker = new DatePicker();
        prescriptionDatePicker.setPromptText("Prescription date");
        prescriptionDatePicker.setPrefWidth(200);
        prescriptionDatePicker.setId("prescriptionDatePicker");
        
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(4);
        notesArea.setPromptText("Prescription notes");
        notesArea.setId("notesArea");
        
        // Create TableView for prescription items
        TableView<String> itemsTable = new TableView<>();
        itemsTable.setPrefHeight(200);
        itemsTable.setPlaceholder(new Label("No prescription items to display"));
        itemsTable.setId("itemsTable");
        
        TableColumn<String, String> medicationCol = new TableColumn<>("Medication");
        medicationCol.setPrefWidth(150);
        medicationCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().split("\\|")[0]));
        
        TableColumn<String, String> dosageCol = new TableColumn<>("Dosage");
        dosageCol.setPrefWidth(120);
        dosageCol.setCellValueFactory(data -> {
            String[] parts = data.getValue().split("\\|");
            return new javafx.beans.property.SimpleStringProperty(parts.length > 1 ? parts[1] : "");
        });
        
        TableColumn<String, String> durationCol = new TableColumn<>("Duration (Days)");
        durationCol.setPrefWidth(100);
        durationCol.setCellValueFactory(data -> {
            String[] parts = data.getValue().split("\\|");
            return new javafx.beans.property.SimpleStringProperty(parts.length > 2 ? parts[2] : "");
        });
        
        itemsTable.getColumns().addAll(medicationCol, dosageCol, durationCol);

        // Create labels
        Label patientLabel = new Label("Patient:");
        Label doctorLabel = new Label("Doctor:");
        Label dateLabel = new Label("Prescription Date:");
        Label notesLabel = new Label("Notes:");
        Label itemsLabel = new Label("Prescription Items:");

        form.addRow(0, patientLabel, patientCombo);
        form.addRow(1, doctorLabel, doctorCombo);
        form.addRow(2, dateLabel, prescriptionDatePicker);
        form.add(notesLabel, 0, 3);
        form.add(notesArea, 1, 3);
        form.add(itemsLabel, 0, 4);
        GridPane.setColumnSpan(itemsLabel, 2);
        form.add(itemsTable, 0, 5);
        GridPane.setColumnSpan(itemsTable, 2);
        
        return form;
    }
    
    private void loadPatients(ComboBox<Integer> patientCombo) {
        try {
            List<Patient> patients = controller.getPatientService().searchPatients("", 1000, 0, "Name (A-Z)");
            List<Integer> patientIds = patients.stream()
                    .map(Patient::getId)
                    .collect(Collectors.toList());
            patientCombo.setItems(FXCollections.observableArrayList(patientIds));
            
            // Custom display for patient combo (ID: Name)
            patientCombo.setCellFactory(lv -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer id, boolean empty) {
                    super.updateItem(id, empty);
                    if (empty || id == null) {
                        setText(null);
                    } else {
                        try {
                            controller.getPatientService().getPatientById(id).ifPresent(p -> 
                                setText(id + ": " + p.getFirstName() + " " + p.getLastName())
                            );
                        } catch (SQLException e) {
                            setText(String.valueOf(id));
                        }
                    }
                }
            });
            
            patientCombo.setButtonCell(new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer id, boolean empty) {
                    super.updateItem(id, empty);
                    if (empty || id == null) {
                        setText(null);
                    } else {
                        try {
                            controller.getPatientService().getPatientById(id).ifPresent(p -> 
                                setText(id + ": " + p.getFirstName() + " " + p.getLastName())
                            );
                        } catch (SQLException e) {
                            setText(String.valueOf(id));
                        }
                    }
                }
            });
        } catch (SQLException ex) {
            System.err.println("Failed to load patients: " + ex.getMessage());
        }
    }
    
    private void loadDoctors(ComboBox<Integer> doctorCombo) {
        try {
            List<Doctor> doctors = controller.getDoctorService().searchDoctors("", 1000, 0, "Name (A-Z)");
            List<Integer> doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toList());
            doctorCombo.setItems(FXCollections.observableArrayList(doctorIds));
            
            // Custom display for doctor combo (ID: Name)
            doctorCombo.setCellFactory(lv -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer id, boolean empty) {
                    super.updateItem(id, empty);
                    if (empty || id == null) {
                        setText(null);
                    } else {
                        try {
                            controller.getDoctorService().getDoctorById(id).ifPresent(d -> 
                                setText(id + ": " + d.getFirstName() + " " + d.getLastName())
                            );
                        } catch (SQLException e) {
                            setText(String.valueOf(id));
                        }
                    }
                }
            });
            
            doctorCombo.setButtonCell(new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer id, boolean empty) {
                    super.updateItem(id, empty);
                    if (empty || id == null) {
                        setText(null);
                    } else {
                        try {
                            controller.getDoctorService().getDoctorById(id).ifPresent(d -> 
                                setText(id + ": " + d.getFirstName() + " " + d.getLastName())
                            );
                        } catch (SQLException e) {
                            setText(String.valueOf(id));
                        }
                    }
                }
            });
        } catch (SQLException ex) {
            System.err.println("Failed to load doctors: " + ex.getMessage());
        }
    }
    
    private void wireEventHandlers(TableView<Prescription> table, Pagination pagination,
                                   TextField searchField, Button searchBtn, ComboBox<String> sortCombo,
                                   Label feedback, Button saveBtn, Button deleteBtn, Button clearBtn,
                                   ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo, 
                                   DatePicker prescriptionDatePicker, TextArea notesArea,
                                   TableView<String> itemsTable) {
        
        // Search handlers
        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadPrescriptions(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadPrescriptions(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        
        // Sort handler
        sortCombo.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadPrescriptions(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        
        // Pagination handler
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> 
                controller.loadPrescriptions(table, pagination, searchField.getText(), sortCombo.getValue(), feedback));

        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, prescription) -> {
            controller.populateForm(prescription, patientCombo, doctorCombo, prescriptionDatePicker, notesArea);
            if (prescription != null && prescription.getId() != null) {
                loadPrescriptionItems(prescription.getId(), itemsTable);
            } else {
                itemsTable.getItems().clear();
            }
        });

        // Action button handlers
        saveBtn.setOnAction(e -> controller.savePrescription(table, pagination, searchField, sortCombo, feedback, 
                patientCombo, doctorCombo, prescriptionDatePicker, notesArea));
        
        deleteBtn.setOnAction(e -> controller.deletePrescription(table, pagination, searchField, sortCombo, feedback, 
                patientCombo, doctorCombo, prescriptionDatePicker, notesArea));
        
        clearBtn.setOnAction(e -> {
            controller.clearForm(patientCombo, doctorCombo, prescriptionDatePicker, notesArea);
            table.getSelectionModel().clearSelection();
            itemsTable.getItems().clear();
            feedback.setText("");
        });
    }
    
    private void loadPrescriptionItems(int prescriptionId, TableView<String> itemsTable) {
        try {
            // Query prescription items from database
            String query = "SELECT mi.Name, pi.Dosage, pi.DurationDays " +
                          "FROM PrescriptionItem pi " +
                          "JOIN MedicalInventory mi ON pi.MedicalInventoryID = mi.ID " +
                          "WHERE pi.PrescriptionID = ?";
            
            List<String> items = new java.util.ArrayList<>();
            try (java.sql.Connection conn = Database.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, prescriptionId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String medication = rs.getString("Name");
                        String dosage = rs.getString("Dosage");
                        int duration = rs.getInt("DurationDays");
                        // Format: Medication|Dosage|Duration
                        items.add(medication + "|" + dosage + "|" + duration);
                    }
                }
            }
            itemsTable.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException ex) {
            System.err.println("Failed to load prescription items: " + ex.getMessage());
            itemsTable.getItems().clear();
        }
    }
}
