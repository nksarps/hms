package com.nks.hms.controller;

import com.nks.hms.model.Doctor;
import com.nks.hms.service.IDoctorService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for doctor UI operations.
 * Separates UI logic from Main class (Single Responsibility Principle).
 * Depends on IDoctorService abstraction (Dependency Inversion Principle).
 */
public class DoctorController {
    private static final int PAGE_SIZE = 25;
    private final IDoctorService doctorService;
    
    public DoctorController(IDoctorService doctorService) {
        this.doctorService = doctorService;
    }
    
    /**
     * Loads doctors into table with pagination and sorting.
     */
    public void loadDoctors(TableView<Doctor> table, Pagination pagination,
                           String searchTerm, String sortBy, Label feedback) {
        try {
            int total = doctorService.countDoctors(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            
            List<Doctor> doctors = doctorService.searchDoctors(searchTerm, PAGE_SIZE, offset, sortBy);
            table.setItems(FXCollections.observableArrayList(doctors));
            
            String searchType = isNumeric(searchTerm) ? " (ID search)" : "";
            String cacheInfo = " | Cache: " + doctorService.getCacheStats();
            feedback.setText(total + " doctors found" + searchType + cacheInfo);
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load doctors: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Saves or updates a doctor.
     */
    public void saveDoctor(TableView<Doctor> table, Pagination pagination,
                          TextField searchField, ComboBox<String> sortCombo, Label feedback,
                          TextField firstNameField, TextField lastNameField,
                          TextField phoneField, TextField emailField) {
        Doctor selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        
        if (isNew) {
            selected = new Doctor();
        }
        
        selected.setFirstName(firstNameField.getText());
        selected.setLastName(lastNameField.getText());
        selected.setPhone(phoneField.getText());
        selected.setEmail(emailField.getText());
        
        try {
            int id = doctorService.saveDoctor(selected);
            selected.setId(id);
            feedback.setText(isNew ? "Doctor saved" : "Doctor updated");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadDoctors(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(firstNameField, lastNameField, phoneField, emailField);
            table.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            feedback.setText(ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        } catch (SQLException ex) {
            feedback.setText("Failed to save doctor: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Deletes a doctor.
     */
    public void deleteDoctor(TableView<Doctor> table, Pagination pagination,
                            TextField searchField, ComboBox<String> sortCombo, Label feedback,
                            TextField firstNameField, TextField lastNameField,
                            TextField phoneField, TextField emailField) {
        Doctor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select a doctor to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            doctorService.deleteDoctor(selected.getId());
            feedback.setText("Doctor deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadDoctors(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(firstNameField, lastNameField, phoneField, emailField);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete doctor: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Populates form from selected doctor.
     */
    public void populateForm(Doctor doctor, TextField firstNameField, TextField lastNameField,
                            TextField phoneField, TextField emailField) {
        if (doctor == null) {
            clearForm(firstNameField, lastNameField, phoneField, emailField);
            return;
        }
        firstNameField.setText(doctor.getFirstName());
        lastNameField.setText(doctor.getLastName());
        phoneField.setText(doctor.getPhone());
        emailField.setText(doctor.getEmail());
    }
    
    /**
     * Clears form fields.
     */
    public void clearForm(TextField firstNameField, TextField lastNameField,
                         TextField phoneField, TextField emailField) {
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
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
