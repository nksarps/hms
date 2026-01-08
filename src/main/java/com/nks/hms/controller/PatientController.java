package com.nks.hms.controller;

import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
import com.nks.hms.service.IPatientService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for patient UI operations.
 * Separates UI logic from Main class (Single Responsibility Principle).
 * Depends on IPatientService abstraction (Dependency Inversion Principle).
 */
public class PatientController {
    private static final int PAGE_SIZE = 10;
    private final IPatientService patientService;
    
    public PatientController(IPatientService patientService) {
        this.patientService = patientService;
    }
    
    /**
     * Loads patients into table with pagination and sorting.
     */
    public void loadPatients(TableView<Patient> table, Pagination pagination, 
                            String searchTerm, String sortBy, Label feedback) {
        try {
            int total = patientService.countPatients(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            
            List<Patient> patients = patientService.searchPatients(searchTerm, PAGE_SIZE, offset, sortBy);
            table.setItems(FXCollections.observableArrayList(patients));
            
            String searchType = isNumeric(searchTerm) ? " (ID search)" : "";
            String cacheInfo = " | Cache: " + patientService.getCacheStats();
            feedback.setText(total + " patients found" + searchType + cacheInfo);
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load patients: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Loads visit history for a patient.
     */
    public void loadHistory(Patient patient, TableView<VisitHistory> historyTable, Label feedback) {
        if (patient == null) {
            historyTable.getItems().clear();
            return;
        }
        try {
            List<VisitHistory> visits = patientService.getVisitHistory(patient.getId());
            historyTable.setItems(FXCollections.observableArrayList(visits));
        } catch (SQLException ex) {
            feedback.setText("Failed to load history: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Saves or updates a patient.
     */
    public void savePatient(TableView<Patient> table, Pagination pagination, 
                           TextField searchField, ComboBox<String> sortCombo, Label feedback,
                           TextField firstNameField, TextField middleNameField, TextField lastNameField,
                           DatePicker dobPicker, TextField phoneField, TextField emailField,
                           TextArea addressArea) {
        Patient selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        
        if (isNew) {
            selected = new Patient();
        }
        
        selected.setFirstName(firstNameField.getText());
        selected.setMiddleName(middleNameField.getText());
        selected.setLastName(lastNameField.getText());
        selected.setDateOfBirth(dobPicker.getValue());
        selected.setPhone(phoneField.getText());
        selected.setEmail(emailField.getText());
        selected.setAddress(addressArea.getText());
        
        try {
            int id = patientService.savePatient(selected);
            selected.setId(id);
            feedback.setText(isNew ? "Patient saved" : "Patient updated");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadPatients(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            table.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            feedback.setText(ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        } catch (SQLException ex) {
            feedback.setText("Failed to save patient: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Deletes a patient.
     */
    public void deletePatient(TableView<Patient> table, Pagination pagination,
                             TextField searchField, ComboBox<String> sortCombo, Label feedback,
                             TableView<VisitHistory> historyTable,
                             TextField firstNameField, TextField middleNameField, TextField lastNameField,
                             DatePicker dobPicker, TextField phoneField, TextField emailField,
                             TextArea addressArea) {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select a patient to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            patientService.deletePatient(selected.getId());
            feedback.setText("Patient deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadPatients(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            historyTable.getItems().clear();
            clearForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete patient: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Populates form from selected patient.
     */
    public void populateForm(Patient patient, TextField firstNameField, TextField middleNameField,
                            TextField lastNameField, DatePicker dobPicker, TextField phoneField,
                            TextField emailField, TextArea addressArea) {
        if (patient == null) {
            clearForm(firstNameField, middleNameField, lastNameField, dobPicker, phoneField, emailField, addressArea);
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
    
    /**
     * Clears form fields.
     */
    public void clearForm(TextField firstNameField, TextField middleNameField, TextField lastNameField,
                         DatePicker dobPicker, TextField phoneField, TextField emailField, TextArea addressArea) {
        firstNameField.clear();
        middleNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
    }
    
    private boolean isNumeric(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
