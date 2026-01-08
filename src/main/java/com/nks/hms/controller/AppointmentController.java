package com.nks.hms.controller;

import com.nks.hms.model.Appointment;
import com.nks.hms.model.Doctor;
import com.nks.hms.model.Patient;
import com.nks.hms.service.IDoctorService;
import com.nks.hms.service.IAppointmentService;
import com.nks.hms.service.IPatientService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for appointment UI operations.
 * Separates UI logic from Main class (Single Responsibility Principle).
 * Depends on IAppointmentService abstraction (Dependency Inversion Principle).
 */
public class AppointmentController {
    private static final int MIN_PAGE_SIZE = 10;
    private static final int ESTIMATED_ROW_HEIGHT = 25; // pixels
    private int currentPageSize = MIN_PAGE_SIZE;
    private final IAppointmentService appointmentService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    
    public AppointmentController(IAppointmentService appointmentService, IPatientService patientService, IDoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }
    
    /**
     * Sets the page size dynamically based on available table height.
     * 
     * @param availableHeight Height available for the table in pixels
     */
    public void setDynamicPageSize(double availableHeight) {
        // Calculate rows that can fit: (availableHeight - header height) / row height
        int calculatedSize = Math.max(MIN_PAGE_SIZE, (int) ((availableHeight - 30) / ESTIMATED_ROW_HEIGHT));
        this.currentPageSize = calculatedSize;
    }
    
    /**
     * Gets the current page size.
     * 
     * @return Current page size for pagination
     */
    public int getPageSize() {
        return currentPageSize;
    }
    
    /**
     * Loads appointments into table with pagination and sorting.
     */
    public void loadAppointments(TableView<Appointment> table, Pagination pagination, 
                                String searchTerm, String sortBy, Label feedback) {
        try {
            int pageSize = getPageSize();
            String resolvedSort = sortBy;
            int total = appointmentService.countAppointments(searchTerm, resolvedSort);
            int pages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
            pagination.setPageCount(pages);
            
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * pageSize;
            
            List<Appointment> appointments = appointmentService.searchAppointments(searchTerm, pageSize, offset, resolvedSort);
            table.setItems(FXCollections.observableArrayList(appointments));
            
            String searchType = isNumeric(searchTerm) ? " (ID search)" : "";
            String cacheInfo = " | Cache: " + appointmentService.getCacheStats();
            feedback.setText(total + " appointments found" + searchType + cacheInfo);
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load appointments: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Saves or updates an appointment.
     */
    public void saveAppointment(TableView<Appointment> table, Pagination pagination, 
                               TextField searchField, ComboBox<String> sortCombo, Label feedback,
                               ComboBox<Patient> patientCombo, ComboBox<Doctor> doctorCombo,
                               DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner,
                               TextArea reasonArea) {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        
        if (isNew) {
            selected = new Appointment();
        }
        
        Patient selectedPatient = patientCombo.getValue();
        Doctor selectedDoctor = doctorCombo.getValue();
        
        if (selectedPatient == null || selectedDoctor == null || datePicker.getValue() == null) {
            feedback.setText("Please select patient, doctor, and appointment date");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        selected.setPatientId(selectedPatient.getId());
        selected.setDoctorId(selectedDoctor.getId());
        selected.setPatientName(selectedPatient.getFirstName() + " " + selectedPatient.getLastName());
        selected.setDoctorName(selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName());
        selected.setAppointmentDate(LocalDateTime.of(
            datePicker.getValue(),
            java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
        ));
        selected.setReason(reasonArea.getText());
        
        try {
            int id = appointmentService.saveAppointment(selected);
            selected.setId(id);
            feedback.setText(isNew ? "Appointment scheduled" : "Appointment updated");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadAppointments(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea);
            table.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            feedback.setText(ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        } catch (SQLException ex) {
            feedback.setText("Failed to save appointment: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Deletes an appointment.
     */
    public void deleteAppointment(TableView<Appointment> table, Pagination pagination,
                                 TextField searchField, ComboBox<String> sortCombo, Label feedback,
                                 ComboBox<Patient> patientCombo, ComboBox<Doctor> doctorCombo,
                                 DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner,
                                 TextArea reasonArea) {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select an appointment to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            appointmentService.deleteAppointment(selected.getId());
            feedback.setText("Appointment deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadAppointments(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete appointment: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Populates form from selected appointment.
     */
    public void populateForm(Appointment appointment, ComboBox<Patient> patientCombo, ComboBox<Doctor> doctorCombo,
                            DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner,
                            TextArea reasonArea) {
        if (appointment == null) {
            clearForm(patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea);
            return;
        }
        
        try {
            // Find and select patient
            for (int i = 0; i < patientCombo.getItems().size(); i++) {
                if (patientCombo.getItems().get(i).getId().equals(appointment.getPatientId())) {
                    patientCombo.getSelectionModel().select(i);
                    break;
                }
            }
            
            // Find and select doctor
            for (int i = 0; i < doctorCombo.getItems().size(); i++) {
                if (doctorCombo.getItems().get(i).getId().equals(appointment.getDoctorId())) {
                    doctorCombo.getSelectionModel().select(i);
                    break;
                }
            }
            
            datePicker.setValue(appointment.getAppointmentDate().toLocalDate());
            hourSpinner.getValueFactory().setValue(appointment.getAppointmentDate().getHour());
            minuteSpinner.getValueFactory().setValue(appointment.getAppointmentDate().getMinute());
            reasonArea.setText(appointment.getReason());
        } catch (Exception ex) {
            // If error occurs, clear form
            clearForm(patientCombo, doctorCombo, datePicker, hourSpinner, minuteSpinner, reasonArea);
        }
    }
    
    /**
     * Clears form fields.
     */
    public void clearForm(ComboBox<Patient> patientCombo, ComboBox<Doctor> doctorCombo,
                         DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner,
                         TextArea reasonArea) {
        patientCombo.getSelectionModel().clearSelection();
        doctorCombo.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        hourSpinner.getValueFactory().setValue(9);
        minuteSpinner.getValueFactory().setValue(0);
        reasonArea.clear();
    }
    
    /**
     * Loads list of patients for dropdown.
     */
    public void loadPatients(ComboBox<Patient> patientCombo, Label feedback) {
        try {
            List<Patient> patients = patientService.searchPatients("", 1000, 0, "NAME_ASC");
            patientCombo.setItems(FXCollections.observableArrayList(patients));
        } catch (SQLException ex) {
            feedback.setText("Failed to load patients: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Loads list of doctors for dropdown.
     */
    public void loadDoctors(ComboBox<Doctor> doctorCombo, Label feedback) {
        try {
            List<Doctor> doctors = doctorService.searchDoctors("", 1000, 0, "NAME_ASC");
            doctorCombo.setItems(FXCollections.observableArrayList(doctors));
        } catch (SQLException ex) {
            feedback.setText("Failed to load doctors: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
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
