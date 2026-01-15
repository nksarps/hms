package com.nks.hms.ui;

import com.nks.hms.controller.PatientNoteController;
import com.nks.hms.model.Doctor;
import com.nks.hms.model.Patient;
import com.nks.hms.model.PatientNote;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builder for patient notes management tab (NoSQL/MongoDB).
 * 
 * <p>Demonstrates NoSQL database integration in JavaFX UI.
 * This tab allows users to:
 * <ul>
 *   <li>Select a patient and doctor</li>
 *   <li>Create unstructured patient notes</li>
 *   <li>View notes with full-text search</li>
 *   <li>Compare with relational approach</li>
 * </ul>
 */
public class PatientNoteTabBuilder {
    private final PatientNoteController controller;
    
    public PatientNoteTabBuilder(PatientNoteController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete patient notes management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Patient Notes");
        tab.setClosable(false);
        
        // Configure main table
        TableView<PatientNote> table = buildNotesTable();
        
        // Search controls (left side only)
        TextField searchField = new TextField();
        searchField.setPromptText("Search notes (full-text search)...");
        searchField.setPrefWidth(300);
        Button searchBtn = new Button("Search");
        
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        // Filter controls (will be on right side)
        ComboBox<Patient> filterPatientCombo = new ComboBox<>();
        filterPatientCombo.setPromptText("All patients");
        filterPatientCombo.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<Doctor> filterDoctorCombo = new ComboBox<>();
        filterDoctorCombo.setPromptText("All doctors");
        filterDoctorCombo.setMaxWidth(Double.MAX_VALUE);
        
        Button clearFilterBtn = new Button("Clear Filters");
        clearFilterBtn.setMaxWidth(Double.MAX_VALUE);
        
        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");
        
        // Form fields
        GridPane form = buildForm();
        @SuppressWarnings("unchecked")
        ComboBox<Patient> patientCombo = (ComboBox<Patient>) form.lookup("#patientCombo");
        @SuppressWarnings("unchecked")
        ComboBox<Doctor> doctorCombo = (ComboBox<Doctor>) form.lookup("#doctorCombo");
        @SuppressWarnings("unchecked")
        ComboBox<String> noteTypeCombo = (ComboBox<String>) form.lookup("#noteTypeCombo");
        TextArea contentArea = (TextArea) form.lookup("#contentArea");
        
        // Load initial data
        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);
        
        // Action buttons
        Button saveBtn = new Button("Save / Update");
        saveBtn.setStyle("-fx-font-weight: bold;");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        HBox actionButtons = new HBox(10, saveBtn, deleteBtn, clearBtn);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        
        // Right pane with filters at top
        Label filterTitle = new Label("FILTERS");
        filterTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        Label patientFilterLabel = new Label("Patient:");
        patientFilterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        
        Label doctorFilterLabel = new Label("Doctor:");
        doctorFilterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        
        VBox filterSection = new VBox(10,
            filterTitle,
            new Separator(),
            patientFilterLabel,
            filterPatientCombo,
            doctorFilterLabel,
            filterDoctorCombo,
            clearFilterBtn
        );
        filterSection.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 5; " +
                              "-fx-background-radius: 5; -fx-padding: 15; -fx-background-color: #fafafa;");
        
        Label formTitle = new Label("NOTE DETAILS");
        formTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        VBox formSection = new VBox(8, formTitle, new Separator(), form);
        
        VBox rightPaneContent = new VBox(15, filterSection, formSection, actionButtons, feedback);
        rightPaneContent.setPadding(new Insets(10));
        
        // Wrap right pane in ScrollPane
        ScrollPane rightScrollPane = new ScrollPane(rightPaneContent);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScrollPane.setPrefWidth(450);
        rightScrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox leftPane = new VBox(10, searchBox, table, pagination);
        VBox.setVgrow(table, Priority.ALWAYS);
        leftPane.setPadding(new Insets(10));
        
        BorderPane layout = new BorderPane();
        layout.setCenter(leftPane);
        layout.setRight(rightScrollPane);
        
        tab.setContent(layout);
        
        // Wire up event handlers
        wireEventHandlers(table, pagination, searchField, searchBtn, filterPatientCombo, 
                         filterDoctorCombo, clearFilterBtn, feedback, saveBtn, deleteBtn, clearBtn,
                         patientCombo, doctorCombo, noteTypeCombo, contentArea);
        
        return tab;
    }
    
    private TableView<PatientNote> buildNotesTable() {
        TableView<PatientNote> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<PatientNote, String> idCol = new TableColumn<>("Note ID");
        idCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getId() != null ? 
                cellData.getValue().getId().toHexString().substring(18) : ""));
        idCol.setPrefWidth(80);
        
        TableColumn<PatientNote, Integer> patientCol = new TableColumn<>("Patient ID");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientCol.setPrefWidth(80);
        
        TableColumn<PatientNote, Integer> doctorCol = new TableColumn<>("Doctor ID");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        doctorCol.setPrefWidth(80);
        
        TableColumn<PatientNote, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("noteType"));
        typeCol.setPrefWidth(100);
        
        TableColumn<PatientNote, String> timestampCol = new TableColumn<>("Date, Time");
        timestampCol.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            if (timestamp != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
                return new javafx.beans.property.SimpleStringProperty(timestamp.format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        timestampCol.setPrefWidth(170);
        
        TableColumn<PatientNote, String> contentCol = new TableColumn<>("Content Preview");
        contentCol.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getContent();
            String preview = content != null && content.length() > 50 ? 
                content.substring(0, 50) + "..." : content;
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        
        table.getColumns().addAll(idCol, patientCol, doctorCol, typeCol, timestampCol, contentCol);
        return table;
    }
    
    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));
        
        // Patient field with label above
        Label patientLabel = new Label("Patient:");
        patientLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        ComboBox<Patient> patientCombo = new ComboBox<>();
        patientCombo.setId("patientCombo");
        patientCombo.setMaxWidth(Double.MAX_VALUE);
        patientCombo.setPromptText("Choose a patient...");
        
        // Doctor field with label above
        Label doctorLabel = new Label("Doctor:");
        doctorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        ComboBox<Doctor> doctorCombo = new ComboBox<>();
        doctorCombo.setId("doctorCombo");
        doctorCombo.setMaxWidth(Double.MAX_VALUE);
        doctorCombo.setPromptText("Choose a doctor...");
        
        // Note Type field with label above
        Label noteTypeLabel = new Label("Note Type:");
        noteTypeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        ComboBox<String> noteTypeCombo = new ComboBox<>();
        noteTypeCombo.setId("noteTypeCombo");
        noteTypeCombo.setItems(FXCollections.observableArrayList(
            "Clinical", "Discharge", "Progress", "Consultation", "Emergency", "Follow-up"
        ));
        noteTypeCombo.setMaxWidth(Double.MAX_VALUE);
        noteTypeCombo.setPromptText("Select note type...");
        
        // Content field with label above
        Label contentLabel = new Label("Content:");
        contentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextArea contentArea = new TextArea();
        contentArea.setId("contentArea");
        contentArea.setPrefRowCount(10);
        contentArea.setWrapText(true);
        contentArea.setPromptText("Enter detailed patient notes here...\n\nNoSQL allows flexible, unstructured content without schema constraints.");
        
        // Add components in vertical layout (labels above fields)
        int row = 0;
        form.add(patientLabel, 0, row++);
        form.add(patientCombo, 0, row++);
        form.add(doctorLabel, 0, row++);
        form.add(doctorCombo, 0, row++);
        form.add(noteTypeLabel, 0, row++);
        form.add(noteTypeCombo, 0, row++);
        form.add(contentLabel, 0, row++);
        form.add(contentArea, 0, row);
        
        // Set growth priorities
        GridPane.setVgrow(contentArea, Priority.ALWAYS);
        GridPane.setHgrow(patientCombo, Priority.ALWAYS);
        GridPane.setHgrow(doctorCombo, Priority.ALWAYS);
        GridPane.setHgrow(noteTypeCombo, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);
        
        return form;
    }
    
    /**
     * Wires up events for the patient notes tab.
     */
    private void wireEventHandlers(TableView<PatientNote> table,
                                   Pagination pagination,
                                   TextField searchField,
                                   Button searchBtn,
                                   ComboBox<Patient> filterPatientCombo,
                                   ComboBox<Doctor> filterDoctorCombo,
                                   Button clearFilterBtn,
                                   Label feedback,
                                   Button saveBtn,
                                   Button deleteBtn,
                                   Button clearBtn,
                                   ComboBox<Patient> patientCombo,
                                   ComboBox<Doctor> doctorCombo,
                                   ComboBox<String> noteTypeCombo,
                                   TextArea contentArea) {
        
        // Load patients and doctors for filters and form
        controller.loadPatients(filterPatientCombo, feedback);
        controller.loadDoctors(filterDoctorCombo, feedback);
        controller.loadPatients(patientCombo, feedback);
        controller.loadDoctors(doctorCombo, feedback);
        
        // Initial load of notes
        controller.loadNotes(table, pagination, searchField.getText(), 
                           filterPatientCombo.getValue(), filterDoctorCombo.getValue(), feedback);
        
        // Search handlers
        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadNotes(table, pagination, searchField.getText(), 
                               filterPatientCombo.getValue(), filterDoctorCombo.getValue(), feedback);
        });
        
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadNotes(table, pagination, searchField.getText(), 
                               filterPatientCombo.getValue(), filterDoctorCombo.getValue(), feedback);
        });
        
        // Filter handlers
        filterPatientCombo.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadNotes(table, pagination, searchField.getText(), 
                               filterPatientCombo.getValue(), filterDoctorCombo.getValue(), feedback);
        });
        
        filterDoctorCombo.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadNotes(table, pagination, searchField.getText(), 
                               filterPatientCombo.getValue(), filterDoctorCombo.getValue(), feedback);
        });
        
        // Clear filters
        clearFilterBtn.setOnAction(e -> {
            filterPatientCombo.setValue(null);
            filterDoctorCombo.setValue(null);
            searchField.clear();
            pagination.setCurrentPageIndex(0);
            controller.loadNotes(table, pagination, "", null, null, feedback);
        });
        
        // Pagination handler
        pagination.currentPageIndexProperty().addListener((obs, old, idx) ->
                controller.loadNotes(table, pagination, searchField.getText(), 
                                   filterPatientCombo.getValue(), filterDoctorCombo.getValue(), feedback));
        
        // Table selection handler - populate form for editing
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, note) -> {
            if (note != null) {
                controller.populateFormForEdit(note, patientCombo, doctorCombo, noteTypeCombo, contentArea);
            }
        });
        
        // Action button handlers
        saveBtn.setOnAction(e -> {
            PatientNote selectedNote = table.getSelectionModel().getSelectedItem();
            
            if (selectedNote != null) {
                // Update existing note
                controller.updateNote(selectedNote, patientCombo, doctorCombo, noteTypeCombo,
                                    contentArea, table, pagination, searchField, feedback);
            } else {
                // Create new note
                controller.saveNote(patientCombo, doctorCombo, noteTypeCombo, contentArea,
                                  table, pagination, searchField, feedback);
            }
            
            // Clear selection after save/update
            table.getSelectionModel().clearSelection();
        });
        
        deleteBtn.setOnAction(e -> {
            controller.deleteNote(table, pagination, searchField, feedback);
            controller.clearForm(patientCombo, doctorCombo, noteTypeCombo, contentArea);
            feedback.setText("");
        });
        
        clearBtn.setOnAction(e -> {
            controller.clearForm(patientCombo, doctorCombo, noteTypeCombo, contentArea);
            table.getSelectionModel().clearSelection();
            feedback.setText("");
        });
    }
}
