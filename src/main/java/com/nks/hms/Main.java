package com.nks.hms;

import com.nks.hms.model.Doctor;
import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
import com.nks.hms.repository.DoctorRepository;
import com.nks.hms.repository.PatientRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int PAGE_SIZE = 10;
    private final PatientRepository patientRepository = new PatientRepository();
    private final DoctorRepository doctorRepository = new DoctorRepository();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hospital Management - Data Access");
        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildPatientTab());
        tabs.getTabs().add(buildDoctorTab());
        stage.setScene(new Scene(tabs, 1200, 760));
        stage.show();
    }

    private Tab buildPatientTab() {
        Tab tab = new Tab("Patients");
        tab.setClosable(false);

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

        TableView<VisitHistory> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<VisitHistory, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        TableColumn<VisitHistory, LocalDate> visitDateCol = new TableColumn<>("Visit Date");
        visitDateCol.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        TableColumn<VisitHistory, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        TableColumn<VisitHistory, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        historyTable.getColumns().addAll(doctorCol, visitDateCol, reasonCol, notesCol);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, phone, or email");
        Button searchBtn = new Button("Search");
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

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

        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            loadPatients(table, pagination, searchField.getText(), feedback);
        });
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            loadPatients(table, pagination, searchField.getText(), feedback);
        });
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> loadPatients(table, pagination, searchField.getText(), feedback));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, patient) -> {
            populatePatientForm(patient, firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            loadHistory(patient, historyTable, feedback);
        });

        saveBtn.setOnAction(e -> handleSavePatient(table, pagination, searchField, feedback, firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea));
        deleteBtn.setOnAction(e -> handleDeletePatient(table, pagination, searchField, feedback, historyTable, firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea));
        clearBtn.setOnAction(e -> {
            clearPatientForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            table.getSelectionModel().clearSelection();
            historyTable.getItems().clear();
            feedback.setText("");
        });

        loadPatients(table, pagination, "", feedback);
        return tab;
    }

    private Tab buildDoctorTab() {
        Tab tab = new Tab("Doctors");
        tab.setClosable(false);

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

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, phone, or email");
        Button searchBtn = new Button("Search");
        HBox searchBox = new HBox(8, new Label("Search:"), searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Pagination pagination = new Pagination(1, 0);
        pagination.setMaxPageIndicatorCount(10);

        Label feedback = new Label();
        feedback.setStyle("-fx-text-fill: #006400;");

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

        searchBtn.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            loadDoctors(table, pagination, searchField.getText(), feedback);
        });
        searchField.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            loadDoctors(table, pagination, searchField.getText(), feedback);
        });
        pagination.currentPageIndexProperty().addListener((obs, old, idx) -> loadDoctors(table, pagination, searchField.getText(), feedback));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, doctor) -> populateDoctorForm(doctor, firstNameField, lastNameField, phoneField, emailField));

        saveBtn.setOnAction(e -> handleSaveDoctor(table, pagination, searchField, feedback, firstNameField, lastNameField, phoneField, emailField));
        deleteBtn.setOnAction(e -> handleDeleteDoctor(table, pagination, searchField, feedback, firstNameField, lastNameField, phoneField, emailField));
        clearBtn.setOnAction(e -> {
            clearDoctorForm(firstNameField, lastNameField, phoneField, emailField);
            table.getSelectionModel().clearSelection();
            feedback.setText("");
        });

        loadDoctors(table, pagination, "", feedback);
        return tab;
    }

    private void loadPatients(TableView<Patient> table, Pagination pagination, String searchTerm, Label feedback) {
        try {
            int total = patientRepository.count(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            List<Patient> patients = patientRepository.find(searchTerm, PAGE_SIZE, offset);
            table.setItems(FXCollections.observableArrayList(patients));
            feedback.setText(total + " patients found");
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load patients: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private void loadDoctors(TableView<Doctor> table, Pagination pagination, String searchTerm, Label feedback) {
        try {
            int total = doctorRepository.count(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            List<Doctor> doctors = doctorRepository.find(searchTerm, PAGE_SIZE, offset);
            table.setItems(FXCollections.observableArrayList(doctors));
            feedback.setText(total + " doctors found");
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load doctors: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private void populatePatientForm(Patient patient, TextField firstNameField, TextField middleNameField, TextField lastNameField, DatePicker dobPicker, TextField phoneField, TextField emailField, TextArea addressArea) {
        if (patient == null) {
            clearPatientForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            return;
        }
        firstNameField.setText(patient.getFirstName());
        middleNameField.setText(patient.getMiddleName());
        lastNameField.setText(patient.getLastName());
        dobPicker.setValue(patient.getDateOfBirth());
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        addressArea.setText(patient.getAddress());
    }

    private void populateDoctorForm(Doctor doctor, TextField firstNameField, TextField lastNameField, TextField phoneField, TextField emailField) {
        if (doctor == null) {
            clearDoctorForm(firstNameField, lastNameField, phoneField, emailField);
            return;
        }
        firstNameField.setText(doctor.getFirstName());
        lastNameField.setText(doctor.getLastName());
        phoneField.setText(doctor.getPhone());
        emailField.setText(doctor.getEmail());
    }

    private void clearPatientForm(TextField firstNameField, TextField middleNameField, TextField lastNameField, DatePicker dobPicker, TextField phoneField, TextField emailField, TextArea addressArea) {
        firstNameField.clear();
        middleNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
    }

    private void clearDoctorForm(TextField firstNameField, TextField lastNameField, TextField phoneField, TextField emailField) {
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private void handleSavePatient(TableView<Patient> table, Pagination pagination, TextField searchField, Label feedback, TextField firstNameField, TextField middleNameField, TextField lastNameField, DatePicker dobPicker, TextField phoneField, TextField emailField, TextArea addressArea) {
        Optional<String> validationError = validatePatient(firstNameField.getText(), lastNameField.getText(), dobPicker.getValue(), phoneField.getText(), emailField.getText());
        if (validationError.isPresent()) {
            feedback.setText(validationError.get());
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }

        Patient selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        if (isNew) {
            selected = new Patient();
        }
        selected.setFirstName(firstNameField.getText().trim());
        selected.setMiddleName(middleNameField.getText().trim());
        selected.setLastName(lastNameField.getText().trim());
        selected.setDateOfBirth(dobPicker.getValue());
        selected.setPhone(phoneField.getText().trim());
        selected.setEmail(emailField.getText().trim());
        selected.setAddress(addressArea.getText());

        try {
            if (isNew) {
                int id = patientRepository.insert(selected);
                selected.setId(id);
                feedback.setText("Patient saved");
            } else {
                patientRepository.update(selected);
                feedback.setText("Patient updated");
            }
            feedback.setStyle("-fx-text-fill: #006400;");
            loadPatients(table, pagination, searchField.getText(), feedback);
            clearPatientForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to save patient: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private void handleDeletePatient(TableView<Patient> table, Pagination pagination, TextField searchField, Label feedback, TableView<VisitHistory> historyTable, TextField firstNameField, TextField middleNameField, TextField lastNameField, DatePicker dobPicker, TextField phoneField, TextField emailField, TextArea addressArea) {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select a patient to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        try {
            patientRepository.delete(selected.getId());
            feedback.setText("Patient deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadPatients(table, pagination, searchField.getText(), feedback);
            historyTable.getItems().clear();
            clearPatientForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete patient: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private void handleSaveDoctor(TableView<Doctor> table, Pagination pagination, TextField searchField, Label feedback, TextField firstNameField, TextField lastNameField, TextField phoneField, TextField emailField) {
        Optional<String> validationError = validateDoctor(firstNameField.getText(), lastNameField.getText(), phoneField.getText(), emailField.getText());
        if (validationError.isPresent()) {
            feedback.setText(validationError.get());
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }

        Doctor selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        if (isNew) {
            selected = new Doctor();
        }
        selected.setFirstName(firstNameField.getText().trim());
        selected.setLastName(lastNameField.getText().trim());
        selected.setPhone(phoneField.getText().trim());
        selected.setEmail(emailField.getText().trim());

        try {
            if (isNew) {
                int id = doctorRepository.insert(selected);
                selected.setId(id);
                feedback.setText("Doctor saved");
            } else {
                doctorRepository.update(selected);
                feedback.setText("Doctor updated");
            }
            feedback.setStyle("-fx-text-fill: #006400;");
            loadDoctors(table, pagination, searchField.getText(), feedback);
            clearDoctorForm(firstNameField, lastNameField, phoneField, emailField);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to save doctor: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private void handleDeleteDoctor(TableView<Doctor> table, Pagination pagination, TextField searchField, Label feedback, TextField firstNameField, TextField lastNameField, TextField phoneField, TextField emailField) {
        Doctor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select a doctor to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        try {
            doctorRepository.delete(selected.getId());
            feedback.setText("Doctor deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadDoctors(table, pagination, searchField.getText(), feedback);
            clearDoctorForm(firstNameField, lastNameField, phoneField, emailField);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete doctor: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private void loadHistory(Patient patient, TableView<VisitHistory> historyTable, Label feedback) {
        if (patient == null) {
            historyTable.getItems().clear();
            return;
        }
        try {
            List<VisitHistory> visits = patientRepository.fetchHistory(patient.getId());
            historyTable.setItems(FXCollections.observableArrayList(visits));
        } catch (SQLException ex) {
            feedback.setText("Failed to load history: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }

    private Optional<String> validatePatient(String firstName, String lastName, LocalDate dob, String phone, String email) {
        if (firstName == null || firstName.isBlank()) {
            return Optional.of("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            return Optional.of("Last name is required");
        }
        if (dob == null) {
            return Optional.of("Date of birth is required");
        }
        if (phone == null || phone.isBlank()) {
            return Optional.of("Phone is required");
        }
        if (phone.length() < 7) {
            return Optional.of("Phone must be at least 7 digits");
        }
        if (email == null || email.isBlank()) {
            return Optional.of("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return Optional.of("Email format is invalid");
        }
        return Optional.empty();
    }

    private Optional<String> validateDoctor(String firstName, String lastName, String phone, String email) {
        if (firstName == null || firstName.isBlank()) {
            return Optional.of("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            return Optional.of("Last name is required");
        }
        if (phone == null || phone.isBlank()) {
            return Optional.of("Phone is required");
        }
        if (phone.length() < 7) {
            return Optional.of("Phone must be at least 7 digits");
        }
        if (email == null || email.isBlank()) {
            return Optional.of("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return Optional.of("Email format is invalid");
        }
        return Optional.empty();
    }
}