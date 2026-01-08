package com.nks.hms.ui;

import com.nks.hms.controller.PatientFeedbackController;
import com.nks.hms.model.PatientFeedback;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder for patient feedback management tab.
 * 
 * <p>Separates UI construction from Main class (Single Responsibility Principle).
 * Encapsulates all patient feedback tab layout and event wiring logic.
 */
public class PatientFeedbackTabBuilder {
    private final PatientFeedbackController controller;
    
    public PatientFeedbackTabBuilder(PatientFeedbackController controller) {
        this.controller = controller;
    }
    
    /**
     * Builds and returns the complete patient feedback management tab.
     * 
     * @return Configured tab with all UI components and event handlers
     */
    public Tab build() {
        Tab tab = new Tab("Patient Feedback");
        tab.setClosable(false);

        // Configure main table
        TableView<PatientFeedback> table = buildFeedbackTable();

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
        ComboBox<Integer> ratingCombo = (ComboBox<Integer>) form.lookup("#ratingCombo");
        TextArea commentsArea = (TextArea) form.lookup("#commentsArea");

        // Load patients and doctors into combo boxes
        loadPatients(patientCombo);
        loadDoctors(doctorCombo);

        // Load initial feedback data into table
        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);
        controller.loadFeedback(table, pagination, "", sortCombo.getValue(), feedback);

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
                         ratingCombo, commentsArea);
        
        return tab;
    }
    
    private TableView<PatientFeedback> buildFeedbackTable() {
        TableView<PatientFeedback> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<PatientFeedback, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<PatientFeedback, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        
        TableColumn<PatientFeedback, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        
        TableColumn<PatientFeedback, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(70);
        
        TableColumn<PatientFeedback, LocalDateTime> dateCol = new TableColumn<>("Feedback Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));
        dateCol.setCellFactory(col -> new TableCell<PatientFeedback, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
                }
            }
        });
        
        TableColumn<PatientFeedback, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        
        @SuppressWarnings("unchecked")
        var columns = new TableColumn[] {idCol, patientCol, doctorCol, ratingCol, dateCol, commentsCol};
        table.getColumns().addAll(columns);
        return table;
    }
    
    private ComboBox<String> buildSortComboBox() {
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
                "All", "Date (Oldest)", "Date (Newest)", 
                "Patient (A-Z)", "Patient (Z-A)", 
                "Doctor (A-Z)", "Doctor (Z-A)",
                "Rating (Low-High)", "Rating (High-Low)"));
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

        ComboBox<Integer> patientCombo = new ComboBox<>();
        patientCombo.setPromptText("Select Patient");
        patientCombo.setPrefWidth(200);
        patientCombo.setId("patientCombo");
        
        ComboBox<Integer> doctorCombo = new ComboBox<>();
        doctorCombo.setPromptText("Select Doctor");
        doctorCombo.setPrefWidth(200);
        doctorCombo.setId("doctorCombo");
        
        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.setPromptText("Select Rating");
        ratingCombo.setPrefWidth(200);
        ratingCombo.setId("ratingCombo");
        ratingCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        
        TextArea commentsArea = new TextArea();
        commentsArea.setPrefRowCount(6);
        commentsArea.setPromptText("Enter feedback comments");
        commentsArea.setId("commentsArea");
        commentsArea.setWrapText(true);

        // Create labels (visible like in Prescriptions)
        Label patientLabel = new Label("Patient:");
        Label doctorLabel = new Label("Doctor:");
        Label ratingLabel = new Label("Rating (1-5):");
        Label commentsLabel = new Label("Comments:");

        form.addRow(0, patientLabel, patientCombo);
        form.addRow(1, doctorLabel, doctorCombo);
        form.addRow(2, ratingLabel, ratingCombo);
        form.add(commentsLabel, 0, 3);
        form.add(commentsArea, 1, 3);
        
        return form;
    }
    
    private void loadPatients(ComboBox<Integer> patientCombo) {
        try {
            String sql = "SELECT ID, FirstName, LastName FROM Patient ORDER BY FirstName, LastName";
            var conn = Database.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
            
            List<Integer> patientIds = new java.util.ArrayList<>();
            java.util.Map<Integer, String> patientNames = new java.util.HashMap<>();
            
            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                patientIds.add(id);
                patientNames.put(id, name);
            }
            
            patientCombo.setItems(FXCollections.observableArrayList(patientIds));
            
            // Custom display for patient combo (ID: Name)
            patientCombo.setCellFactory(lv -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer id, boolean empty) {
                    super.updateItem(id, empty);
                    if (empty || id == null) {
                        setText(null);
                    } else {
                        setText(id + ": " + patientNames.get(id));
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
                        setText(id + ": " + patientNames.get(id));
                    }
                }
            });
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadDoctors(ComboBox<Integer> doctorCombo) {
        try {
            String sql = "SELECT ID, FirstName, LastName FROM Doctor ORDER BY FirstName, LastName";
            var conn = Database.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
            
            List<Integer> doctorIds = new java.util.ArrayList<>();
            java.util.Map<Integer, String> doctorNames = new java.util.HashMap<>();
            
            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                doctorIds.add(id);
                doctorNames.put(id, name);
            }
            
            doctorCombo.setItems(FXCollections.observableArrayList(doctorIds));
            
            // Custom display for doctor combo (ID: Name)
            doctorCombo.setCellFactory(lv -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer id, boolean empty) {
                    super.updateItem(id, empty);
                    if (empty || id == null) {
                        setText(null);
                    } else {
                        setText(id + ": " + doctorNames.get(id));
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
                        setText(id + ": " + doctorNames.get(id));
                    }
                }
            });
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void wireEventHandlers(TableView<PatientFeedback> table, Pagination pagination,
                                   TextField searchField, Button searchBtn, ComboBox<String> sortCombo,
                                   Label feedback, Button saveBtn, Button deleteBtn, Button clearBtn,
                                   ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                                   ComboBox<Integer> ratingCombo, TextArea commentsArea) {
        
        // Search button
        searchBtn.setOnAction(e -> controller.loadFeedback(table, pagination, 
                searchField.getText(), sortCombo.getValue(), feedback));
        
        // Search on Enter key
        searchField.setOnAction(e -> controller.loadFeedback(table, pagination,
                searchField.getText(), sortCombo.getValue(), feedback));
        
        // Sort combo change
        sortCombo.setOnAction(e -> controller.loadFeedback(table, pagination,
                searchField.getText(), sortCombo.getValue(), feedback));
        
        // Pagination
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> 
                controller.loadFeedback(table, pagination, searchField.getText(), 
                        sortCombo.getValue(), feedback));
        
        // Table row selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
                controller.populateForm(newVal, patientCombo, doctorCombo, ratingCombo, commentsArea));
        
        // Save button
        saveBtn.setOnAction(e -> controller.saveFeedback(table, pagination, searchField, sortCombo,
                feedback, patientCombo, doctorCombo, ratingCombo, commentsArea));
        
        // Delete button
        deleteBtn.setOnAction(e -> controller.deleteFeedback(table, pagination, searchField, sortCombo,
                feedback, patientCombo, doctorCombo, ratingCombo, commentsArea));
        
        // Clear button
        clearBtn.setOnAction(e -> {
            controller.clearForm(patientCombo, doctorCombo, ratingCombo, commentsArea);
            table.getSelectionModel().clearSelection();
        });
    }
}
