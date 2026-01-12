package com.nks.hms.ui;

import com.nks.hms.controller.DoctorController;
import com.nks.hms.model.Doctor;
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

/**
 * Builder for doctor management tab.
 * 
 * <p>Separates UI construction from Main class (Single Responsibility Principle).
 * Encapsulates all doctor tab layout and event wiring logic.
 */
public class DoctorTabBuilder {
    private final DoctorController controller;
    
    public DoctorTabBuilder(DoctorController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete doctor management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Doctors");
        tab.setClosable(false);

        // Configure main table
        TableView<Doctor> table = buildDoctorTable();

        // Search and sort controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, phone, email, or ID");
        Button searchBtn = new Button("Search");
        ComboBox<String> sortCombo = buildSortComboBox();
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn, 
                new Label("Sort:"), sortCombo);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

        // Form fields
        GridPane form = buildForm();
        TextField firstNameField = (TextField) form.getChildren().get(1);
        TextField lastNameField = (TextField) form.getChildren().get(3);
        TextField phoneField = (TextField) form.getChildren().get(5);
        TextField emailField = (TextField) form.getChildren().get(7);

        // Load initial doctor data into table
        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);
        controller.loadDoctors(table, pagination, "", sortCombo.getValue(), feedback);

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
                         feedback, saveBtn, deleteBtn, clearBtn, firstNameField, lastNameField,
                         phoneField, emailField);
        
        return tab;
    }
    
    private TableView<Doctor> buildDoctorTable() {
        TableView<Doctor> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Doctor, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Doctor, String> firstCol = new TableColumn<>("First Name");
        firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<Doctor, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        TableColumn<Doctor, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Doctor, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        table.getColumns().addAll(idCol, firstCol, lastCol, phoneCol, emailCol);
        return table;
    }
    
    private ComboBox<String> buildSortComboBox() {
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
                "All", "Name (A-Z)", "Name (Z-A)"));
        sortCombo.setValue("All");
        sortCombo.setPrefWidth(130);
        return sortCombo;
    }
    
    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();

        form.addRow(0, new Label("First Name"), firstNameField, new Label("Last Name"), lastNameField);
        form.addRow(1, new Label("Phone"), phoneField, new Label("Email"), emailField);
        
        return form;
    }
    
    private void wireEventHandlers(TableView<Doctor> table,
                                   Pagination pagination, TextField searchField, Button searchBtn,
                                   ComboBox<String> sortCombo, Label feedback, Button saveBtn,
                                   Button deleteBtn, Button clearBtn, TextField firstNameField,
                                   TextField lastNameField, TextField phoneField, TextField emailField) {
        
        // Search handlers
        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadDoctors(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadDoctors(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        
        // Sort handler
        sortCombo.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            controller.loadDoctors(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
        });
        
        // Pagination handler
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> 
                controller.loadDoctors(table, pagination, searchField.getText(), sortCombo.getValue(), feedback));

        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, doctor) -> {
            controller.populateForm(doctor, firstNameField, lastNameField, phoneField, emailField);
        });

        // Action button handlers
        saveBtn.setOnAction(e -> controller.saveDoctor(table, pagination, searchField, sortCombo, feedback,
                firstNameField, lastNameField, phoneField, emailField));
        
        deleteBtn.setOnAction(e -> controller.deleteDoctor(table, pagination, searchField, sortCombo, feedback,
                firstNameField, lastNameField, phoneField, emailField));
        
        clearBtn.setOnAction(e -> {
            controller.clearForm(firstNameField, lastNameField, phoneField, emailField);
            table.getSelectionModel().clearSelection();
            feedback.setText("");
        });
    }
}
