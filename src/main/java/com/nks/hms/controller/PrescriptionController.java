package com.nks.hms.controller;

import com.nks.hms.model.Prescription;
import com.nks.hms.service.IPrescriptionService;
import com.nks.hms.service.IPatientService;
import com.nks.hms.service.IDoctorService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for prescription UI operations.
 * Separates UI logic from Main class (Single Responsibility Principle).
 * Depends on IPrescriptionService abstraction (Dependency Inversion Principle).
 */
public class PrescriptionController {
    private static final int PAGE_SIZE = 25;
    private final IPrescriptionService prescriptionService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    
    public PrescriptionController(IPrescriptionService prescriptionService, IPatientService patientService, IDoctorService doctorService) {
        this.prescriptionService = prescriptionService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }
    
    /**
     * Loads prescriptions into table with pagination and sorting.
     */
    public void loadPrescriptions(TableView<Prescription> table, Pagination pagination, 
                                 String searchTerm, String sortBy, Label feedback) {
        try {
            int total = prescriptionService.countPrescriptions(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            
            List<Prescription> prescriptions = prescriptionService.searchPrescriptions(searchTerm, PAGE_SIZE, offset, sortBy);
            table.setItems(FXCollections.observableArrayList(prescriptions));
            
            String cacheInfo = " | Cache: " + prescriptionService.getCacheStats();
            feedback.setText(total + " prescriptions found" + cacheInfo);
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load prescriptions: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Saves or updates a prescription.
     */
    public void savePrescription(TableView<Prescription> table, Pagination pagination, 
                                TextField searchField, ComboBox<String> sortCombo, Label feedback,
                                ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                                DatePicker prescriptionDatePicker, TextArea notesArea) {
        Prescription selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        
        if (isNew) {
            selected = new Prescription();
        }
        
        selected.setPatientId(patientCombo.getValue());
        selected.setDoctorId(doctorCombo.getValue());
        selected.setPrescriptionDate(prescriptionDatePicker.getValue());
        selected.setNotes(notesArea.getText());
        
        try {
            int id = prescriptionService.savePrescription(selected);
            selected.setId(id);
            feedback.setText(isNew ? "Prescription saved" : "Prescription updated");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadPrescriptions(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(patientCombo, doctorCombo, prescriptionDatePicker, notesArea);
            table.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            feedback.setText(ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        } catch (SQLException ex) {
            feedback.setText("Failed to save prescription: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Deletes a prescription.
     */
    public void deletePrescription(TableView<Prescription> table, Pagination pagination,
                                  TextField searchField, ComboBox<String> sortCombo, Label feedback,
                                  ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                                  DatePicker prescriptionDatePicker, TextArea notesArea) {
        Prescription selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select a prescription to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            prescriptionService.deletePrescription(selected.getId());
            feedback.setText("Prescription deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadPrescriptions(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(patientCombo, doctorCombo, prescriptionDatePicker, notesArea);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete prescription: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Populates form from selected prescription.
     */
    public void populateForm(Prescription prescription, ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                            DatePicker prescriptionDatePicker, TextArea notesArea) {
        if (prescription == null) {
            clearForm(patientCombo, doctorCombo, prescriptionDatePicker, notesArea);
            return;
        }
        patientCombo.setValue(prescription.getPatientId());
        doctorCombo.setValue(prescription.getDoctorId());
        prescriptionDatePicker.setValue(prescription.getPrescriptionDate());
        notesArea.setText(prescription.getNotes());
    }
    
    /**
     * Clears form fields.
     */
    public void clearForm(ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                         DatePicker prescriptionDatePicker, TextArea notesArea) {
        patientCombo.setValue(null);
        doctorCombo.setValue(null);
        prescriptionDatePicker.setValue(null);
        notesArea.clear();
    }
    
    public IPatientService getPatientService() {
        return patientService;
    }
    
    public IDoctorService getDoctorService() {
        return doctorService;
    }
}
