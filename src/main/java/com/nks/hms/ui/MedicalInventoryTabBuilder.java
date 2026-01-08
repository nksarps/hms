package com.nks.hms.ui;

import com.nks.hms.controller.MedicalInventoryController;
import com.nks.hms.model.MedicalInventory;
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
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Builder for medical inventory management tab.
 * 
 * <p>Separates UI construction from Main class (Single Responsibility Principle).
 * Encapsulates all medical inventory tab layout and event wiring logic.
 */
public class MedicalInventoryTabBuilder {
    private final MedicalInventoryController controller;
    
    public MedicalInventoryTabBuilder(MedicalInventoryController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete medical inventory management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Medical Inventory");
        tab.setClosable(false);

        // Configure main table
        TableView<MedicalInventory> table = buildInventoryTable();

        // Search and sort controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or type");
        Button searchBtn = new Button("Search");
        ComboBox<String> sortCombo = buildSortComboBox();
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn, 
                new Label("Sort:"), sortCombo);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

        // Form fields
        GridPane form = buildForm();
        TextField nameField = (TextField) form.lookup("#nameField");
        TextField typeField = (TextField) form.lookup("#typeField");
        TextField quantityField = (TextField) form.lookup("#quantityField");
        TextField unitField = (TextField) form.lookup("#unitField");
        DatePicker expiryPicker = (DatePicker) form.lookup("#expiryPicker");
        TextField costField = (TextField) form.lookup("#costField");

        // Load initial inventory data into table
        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);
        controller.loadInventory(table, pagination, "", sortCombo.getValue(), feedback);

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
                         feedback, saveBtn, deleteBtn, clearBtn, nameField, typeField, 
                         quantityField, unitField, expiryPicker, costField);
        
        return tab;
    }
    
    private TableView<MedicalInventory> buildInventoryTable() {
        TableView<MedicalInventory> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<MedicalInventory, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<MedicalInventory, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<MedicalInventory, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<MedicalInventory, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(80);
        
        TableColumn<MedicalInventory, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(80);
        
        TableColumn<MedicalInventory, LocalDate> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        
        TableColumn<MedicalInventory, BigDecimal> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(80);
        
        table.getColumns().addAll(idCol, nameCol, typeCol, quantityCol, unitCol, expiryCol, costCol);
        return table;
    }
    
    private ComboBox<String> buildSortComboBox() {
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
                "All", "Name (A-Z)", "Name (Z-A)", 
                "Quantity (Low-High)", "Quantity (High-Low)",
                "Expiry (Soonest)", "Expiry (Latest)"));
        sortCombo.setValue("All");
        sortCombo.setPrefWidth(170);
        return sortCombo;
    }
    
    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        
        // Set column constraints to prevent label truncation (like in Prescriptions)
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(150);
        col1.setPrefWidth(150);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setPrefWidth(250);
        form.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField();
        nameField.setPromptText("Item name");
        nameField.setId("nameField");
        
        TextField typeField = new TextField();
        typeField.setPromptText("Item type/category");
        typeField.setId("typeField");
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity in stock");
        quantityField.setId("quantityField");
        
        TextField unitField = new TextField();
        unitField.setPromptText("e.g., tablets, bottles, boxes");
        unitField.setId("unitField");
        
        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setPromptText("Expiry date");
        expiryPicker.setId("expiryPicker");
        
        TextField costField = new TextField();
        costField.setPromptText("Cost per unit");
        costField.setId("costField");

        // Create labels (visible like in Prescriptions)
        Label nameLabel = new Label("Name:");
        Label typeLabel = new Label("Type:");
        Label quantityLabel = new Label("Quantity:");
        Label unitLabel = new Label("Unit:");
        Label expiryLabel = new Label("Expiry Date:");
        Label costLabel = new Label("Cost:");

        form.addRow(0, nameLabel, nameField);
        form.addRow(1, typeLabel, typeField);
        form.addRow(2, quantityLabel, quantityField);
        form.addRow(3, unitLabel, unitField);
        form.addRow(4, expiryLabel, expiryPicker);
        form.addRow(5, costLabel, costField);
        
        return form;
    }
    
    private void wireEventHandlers(TableView<MedicalInventory> table, Pagination pagination,
                                   TextField searchField, Button searchBtn, ComboBox<String> sortCombo,
                                   Label feedback, Button saveBtn, Button deleteBtn, Button clearBtn,
                                   TextField nameField, TextField typeField, TextField quantityField,
                                   TextField unitField, DatePicker expiryPicker, TextField costField) {
        
        // Search button
        searchBtn.setOnAction(e -> controller.loadInventory(table, pagination, 
                searchField.getText(), sortCombo.getValue(), feedback));
        
        // Search on Enter key
        searchField.setOnAction(e -> controller.loadInventory(table, pagination,
                searchField.getText(), sortCombo.getValue(), feedback));
        
        // Sort combo change
        sortCombo.setOnAction(e -> controller.loadInventory(table, pagination,
                searchField.getText(), sortCombo.getValue(), feedback));
        
        // Pagination
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> 
                controller.loadInventory(table, pagination, searchField.getText(), 
                        sortCombo.getValue(), feedback));
        
        // Table row selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
                controller.populateForm(newVal, nameField, typeField, quantityField, 
                        unitField, expiryPicker, costField));
        
        // Save button
        saveBtn.setOnAction(e -> controller.saveInventory(table, pagination, searchField, sortCombo,
                feedback, nameField, typeField, quantityField, unitField, expiryPicker, costField));
        
        // Delete button
        deleteBtn.setOnAction(e -> controller.deleteInventory(table, pagination, searchField, sortCombo,
                feedback, nameField, typeField, quantityField, unitField, expiryPicker, costField));
        
        // Clear button
        clearBtn.setOnAction(e -> {
            controller.clearForm(nameField, typeField, quantityField, unitField, expiryPicker, costField);
            table.getSelectionModel().clearSelection();
        });
    }
}
