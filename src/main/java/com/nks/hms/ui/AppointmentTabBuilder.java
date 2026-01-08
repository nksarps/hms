package com.nks.hms.ui;

import com.nks.hms.controller.AppointmentController;
import com.nks.hms.model.Appointment;
import com.nks.hms.model.Doctor;
import com.nks.hms.model.Patient;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builder for appointment management tab.
 * 
 * <p>Separates UI construction from Main class (Single Responsibility Principle).
 * Encapsulates all appointment tab layout and event wiring logic.
 */
public class AppointmentTabBuilder {
    private final AppointmentController controller;
    
    public AppointmentTabBuilder(AppointmentController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete appointment management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Appointments");
        tab.setClosable(false);

        // Configure main table
        TableView<Appointment> table = buildAppointmentTable();
        
        // Search and sort controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search by patient, doctor, or reason");
        Button searchBtn = new Button("Search");
        ComboBox<String> sortCombo = buildSortComboBox();
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn, 
                new Label("Sort:"), sortCombo);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

        // Form fields - extracted from buildForm which stores them in order
        FormComponents formComponents = buildForm();
        GridPane form = formComponents.grid;
        ComboBox<Patient> patientCombo = formComponents.patientCombo;
        ComboBox<Doctor> doctorCombo = formComponents.doctorCombo;
        DatePicker datePicker = formComponents.datePicker;
        Spinner<Integer> hourSpinner = formComponents.hourSpinner;
        Spinner<Integer> minuteSpinner = formComponents.minuteSpinner;
        TextArea reasonArea = formComponents.reasonArea;

        Button saveBtn = new Button("Schedule / Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        HBox actionButtons = new HBox(10, saveBtn, deleteBtn, clearBtn);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        VBox rightPane = new VBox(12, form, actionButtons, feedback);
        rightPane.setPadding(new Insets(10));
        rightPane.setPrefWidth(300);
        rightPane.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 1;");
        
        // Make form scrollable if it gets too large
        ScrollPane formScroll = new ScrollPane(rightPane);
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-padding: 0;");

        VBox leftPane = new VBox(10, searchBox, table, pagination);
        VBox.setVgrow(table, Priority.ALWAYS);
        leftPane.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setCenter(leftPane);
        layout.setRight(formScroll);
        tab.setContent(layout);

        // Wire up event handlers
        wireEventHandlers(table, pagination, searchField, searchBtn, sortCombo, 
                         feedback, saveBtn, deleteBtn, clearBtn, patientCombo, doctorCombo,
                         datePicker, hourSpinner, minuteSpinner, reasonArea);

        // Set up dynamic page size based on table height
        table.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 50) { // Only update if table has meaningful height
                controller.setDynamicPageSize(newVal.doubleValue());
                // Reload appointments with new page size
                controller.loadAppointments(table, pagination, "", sortCombo.getValue(), feedback);
            }
        });

        // Load dropdowns
        controller.loadPatients(patientCombo, feedback);
        controller.loadDoctors(doctorCombo, feedback);
        
        // Initial load
        controller.loadAppointments(table, pagination, "", sortCombo.getValue(), feedback);
        
        return tab;
    }
    
    private TableView<Appointment> buildAppointmentTable() {
        TableView<Appointment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Appointment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Appointment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        patientCol.setPrefWidth(150);
        
        TableColumn<Appointment, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        doctorCol.setPrefWidth(150);
        
        TableColumn<Appointment, LocalDateTime> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        dateCol.setCellFactory(col -> new TableCell<Appointment, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        dateCol.setPrefWidth(200);
        
        TableColumn<Appointment, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonCol.setPrefWidth(200);
        
        table.getColumns().addAll(idCol, patientCol, doctorCol, dateCol, reasonCol);
        return table;
    }
    
    private ComboBox<String> buildSortComboBox() {
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
            "All (Newest)", "Today", "Next 7 Days", "Next 30 Days"));
        sortCombo.setValue("All (Newest)");
        sortCombo.setPrefWidth(130);
        return sortCombo;
    }
    
    private FormComponents buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");

        ComboBox<Patient> patientCombo = new ComboBox<>();
        patientCombo.setPromptText("Select Patient");
        patientCombo.setPrefWidth(200);
        patientCombo.setCellFactory(lv -> new PatientListCell());
        patientCombo.setButtonCell(new PatientListCell());

        ComboBox<Doctor> doctorCombo = new ComboBox<>();
        doctorCombo.setPromptText("Select Doctor");
        doctorCombo.setPrefWidth(200);
        doctorCombo.setCellFactory(lv -> new DoctorListCell());
        doctorCombo.setButtonCell(new DoctorListCell());

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(200);
        datePicker.setStyle("-fx-font-size: 12;");
        
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 9);
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(60);
        hourSpinner.setStyle("-fx-font-size: 12;");
        
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0, 15);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(60);
        minuteSpinner.setStyle("-fx-font-size: 12;");
        
        TextArea reasonArea = new TextArea();
        reasonArea.setPrefRowCount(3);
        reasonArea.setPrefWidth(200);
        reasonArea.setWrapText(true);

        // Create styled labels
        Label patientLabel = new Label("Patient:");
        patientLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        Label doctorLabel = new Label("Doctor:");
        doctorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        Label dateLabel = new Label("Appointment Date:");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        Label timeLabel = new Label("Time of Day:");
        timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        Label hourLabel = new Label("Hour (0-23):");
        hourLabel.setStyle("-fx-font-size: 10;");
        Label minuteLabel = new Label("Minutes (0-59):");
        minuteLabel.setStyle("-fx-font-size: 10;");
        Label reasonLabel = new Label("Reason for Appointment:");
        reasonLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");

        // Add to grid with proper constraints
        form.add(patientLabel, 0, 0);
        form.add(patientCombo, 0, 1);
        
        form.add(doctorLabel, 0, 2);
        form.add(doctorCombo, 0, 3);
        
        form.add(dateLabel, 0, 4);
        form.add(datePicker, 0, 5);
        
        form.add(timeLabel, 0, 6);
        HBox timeInputBox = new HBox(8);
        timeInputBox.setAlignment(Pos.CENTER_LEFT);
        VBox hourBox = new VBox(2, hourLabel, hourSpinner);
        VBox minuteBox = new VBox(2, minuteLabel, minuteSpinner);
        Label colonLabel = new Label(":");
        colonLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        timeInputBox.getChildren().addAll(hourBox, colonLabel, minuteBox);
        form.add(timeInputBox, 0, 7);
        
        form.add(reasonLabel, 0, 8);
        form.add(reasonArea, 0, 9);
        
        return new FormComponents(form, patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea);
    }
    
    /**
     * Helper class to return multiple form components.
     */
    private static class FormComponents {
        GridPane grid;
        ComboBox<Patient> patientCombo;
        ComboBox<Doctor> doctorCombo;
        DatePicker datePicker;
        Spinner<Integer> hourSpinner;
        Spinner<Integer> minuteSpinner;
        TextArea reasonArea;
        
        FormComponents(GridPane grid, ComboBox<Patient> patientCombo, ComboBox<Doctor> doctorCombo,
                      DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner,
                      TextArea reasonArea) {
            this.grid = grid;
            this.patientCombo = patientCombo;
            this.doctorCombo = doctorCombo;
            this.datePicker = datePicker;
            this.hourSpinner = hourSpinner;
            this.minuteSpinner = minuteSpinner;
            this.reasonArea = reasonArea;
        }
    }
    
    private void wireEventHandlers(TableView<Appointment> table, Pagination pagination,
                                   TextField searchField, Button searchBtn, ComboBox<String> sortCombo,
                                   Label feedback, Button saveBtn, Button deleteBtn, Button clearBtn,
                                   ComboBox<Patient> patientCombo, ComboBox<Doctor> doctorCombo,
                                   DatePicker datePicker, Spinner<Integer> hourSpinner,
                                   Spinner<Integer> minuteSpinner, TextArea reasonArea) {
        
        // Search handlers
        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadAppointments(table, pagination, searchField.getText(), 
                                       parseSortBy(sortCombo.getValue()), feedback);
        });
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadAppointments(table, pagination, searchField.getText(), 
                                       parseSortBy(sortCombo.getValue()), feedback);
        });
        
        // Sort handler
        sortCombo.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadAppointments(table, pagination, searchField.getText(), 
                                       parseSortBy(sortCombo.getValue()), feedback);
        });
        
        // Pagination handler
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> 
                controller.loadAppointments(table, pagination, searchField.getText(), 
                                           parseSortBy(sortCombo.getValue()), feedback));

        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, appointment) -> 
                controller.populateForm(appointment, patientCombo, doctorCombo, datePicker, 
                                       hourSpinner, minuteSpinner, reasonArea));

        // Action button handlers
        saveBtn.setOnAction(e -> controller.saveAppointment(table, pagination, searchField, 
                sortCombo, feedback, patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea));
        
        deleteBtn.setOnAction(e -> controller.deleteAppointment(table, pagination, searchField, 
                sortCombo, feedback, patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea));
        
        clearBtn.setOnAction(e -> {
            controller.clearForm(patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea);
            table.getSelectionModel().clearSelection();
            feedback.setText("");
        });
    }
    
    /**
     * Converts sort combo box value to service sort string.
     */
    private String parseSortBy(String sortText) {
        if (sortText == null) {
            return "DATE_DESC";
        }
        
        return switch (sortText) {
            case "All (Newest)" -> "DATE_DESC";
            case "Today" -> "TODAY";
            case "Next 7 Days" -> "NEXT_7";
            case "Next 30 Days" -> "NEXT_30";
            default -> "DATE_DESC";
        };
    }
    
    /**
     * Custom cell to display Patient names in combobox.
     */
    private static class PatientListCell extends ListCell<Patient> {
        @Override
        protected void updateItem(Patient patient, boolean empty) {
            super.updateItem(patient, empty);
            if (empty || patient == null) {
                setText(null);
            } else {
                setText(patient.getFirstName() + " " + patient.getLastName() + " (ID: " + patient.getId() + ")");
            }
        }
    }
    
    /**
     * Custom cell to display Doctor names in combobox.
     */
    private static class DoctorListCell extends ListCell<Doctor> {
        @Override
        protected void updateItem(Doctor doctor, boolean empty) {
            super.updateItem(doctor, empty);
            if (empty || doctor == null) {
                setText(null);
            } else {
                setText(doctor.getFirstName() + " " + doctor.getLastName() + " (ID: " + doctor.getId() + ")");
            }
        }
    }
}
