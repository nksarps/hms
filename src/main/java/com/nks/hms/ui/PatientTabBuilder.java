package com.nks.hms.ui;

import com.nks.hms.controller.PatientController;
import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
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
import java.time.LocalDate;

/**
 * Builder for patient management tab.
 * 
 * <p>Separates UI construction from Main class (Single Responsibility Principle).
 * Encapsulates all patient tab layout and event wiring logic.
 */
public class PatientTabBuilder {
    private final PatientController controller;
    
    public PatientTabBuilder(PatientController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete patient management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Patients");
        tab.setClosable(false);

        // Configure main table
        TableView<Patient> table = buildPatientTable();
        
        // Configure visit history table
        TableView<VisitHistory> historyTable = buildHistoryTable();

        // Search and sort controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, phone, email, or ID");
        Button searchBtn = new Button("Search");
        ComboBox<String> sortCombo = buildSortComboBox();
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn, 
                new Label("Sort:"), sortCombo);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

        // Form fields
        GridPane form = buildForm();
        TextField firstNameField = (TextField) form.getChildren().get(1);
        TextField middleNameField = (TextField) form.getChildren().get(3);
        TextField lastNameField = (TextField) form.getChildren().get(5);
        DatePicker dobPicker = (DatePicker) form.getChildren().get(7);
        TextField phoneField = (TextField) form.getChildren().get(9);
        TextField emailField = (TextField) form.getChildren().get(11);
        TextArea addressArea = (TextArea) form.getChildren().get(13);

        Button saveBtn = new Button("Save / Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        HBox actionButtons = new HBox(10, saveBtn, deleteBtn, clearBtn);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        VBox rightPane = new VBox(12, form, actionButtons, feedback);
        rightPane.setPadding(new Insets(10));
        rightPane.setPrefWidth(450);

        VBox leftPane = new VBox(10, searchBox, table, pagination, new Label("Recent Visits"), historyTable);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(historyTable, Priority.SOMETIMES);
        leftPane.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setCenter(leftPane);
        layout.setRight(rightPane);
        tab.setContent(layout);

        // Wire up event handlers
        wireEventHandlers(table, historyTable, pagination, searchField, searchBtn, sortCombo, 
                         feedback, saveBtn, deleteBtn, clearBtn, firstNameField, middleNameField, 
                         lastNameField, dobPicker, phoneField, emailField, addressArea);

        // Initial load
        controller.loadPatients(table, pagination, "", sortCombo.getValue(), feedback);
        
        return tab;
    }
    
    private TableView<Patient> buildPatientTable() {
        TableView<Patient> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Patient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Patient, String> firstCol = new TableColumn<>("First Name");
        firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<Patient, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        TableColumn<Patient, LocalDate> dobCol = new TableColumn<>("DOB");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Patient, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        table.getColumns().addAll(idCol, firstCol, lastCol, dobCol, phoneCol, emailCol);
        return table;
    }
    
    private TableView<VisitHistory> buildHistoryTable() {
        TableView<VisitHistory> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<VisitHistory, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        TableColumn<VisitHistory, LocalDate> visitDateCol = new TableColumn<>("Visit Date");
        visitDateCol.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        TableColumn<VisitHistory, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        TableColumn<VisitHistory, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        table.getColumns().addAll(doctorCol, visitDateCol, reasonCol, notesCol);
        return table;
    }
    
    private ComboBox<String> buildSortComboBox() {
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
                "ID (Newest)", "ID (Oldest)", "Name (A-Z)", "Name (Z-A)", 
                "DOB (Oldest)", "DOB (Newest)"));
        sortCombo.setValue("ID (Newest)");
        sortCombo.setPrefWidth(130);
        return sortCombo;
    }
    
    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        TextField firstNameField = new TextField();
        TextField middleNameField = new TextField();
        TextField lastNameField = new TextField();
        DatePicker dobPicker = new DatePicker();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextArea addressArea = new TextArea();
        addressArea.setPrefRowCount(3);

        form.addRow(0, new Label("First Name"), firstNameField, new Label("Middle Name"), middleNameField);
        form.addRow(1, new Label("Last Name"), lastNameField, new Label("Date of Birth"), dobPicker);
        form.addRow(2, new Label("Phone"), phoneField, new Label("Email"), emailField);
        form.add(new Label("Address"), 0, 3);
        form.add(addressArea, 1, 3, 3, 1);
        
        return form;
    }
    
    private void wireEventHandlers(TableView<Patient> table, TableView<VisitHistory> historyTable,
                                   Pagination pagination, TextField searchField, Button searchBtn,
                                   ComboBox<String> sortCombo, Label feedback, Button saveBtn,
                                   Button deleteBtn, Button clearBtn, TextField firstNameField,
                                   TextField middleNameField, TextField lastNameField, DatePicker dobPicker,
                                   TextField phoneField, TextField emailField, TextArea addressArea) {
        
        // Search handlers
        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadPatients(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadPatients(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        
        // Sort handler
        sortCombo.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadPatients(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        
        // Pagination handler
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> 
                controller.loadPatients(table, pagination, searchField.getText(), sortCombo.getValue(), feedback));

        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, patient) -> {
            controller.populateForm(patient, firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            controller.loadHistory(patient, historyTable, feedback);
        });

        // Action button handlers
        saveBtn.setOnAction(e -> controller.savePatient(table, pagination, searchField, sortCombo, feedback, 
                firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea));
        
        deleteBtn.setOnAction(e -> controller.deletePatient(table, pagination, searchField, sortCombo, feedback, 
                historyTable, firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea));
        
        clearBtn.setOnAction(e -> {
            controller.clearForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            table.getSelectionModel().clearSelection();
            historyTable.getItems().clear();
            feedback.setText("");
        });
    }
}
