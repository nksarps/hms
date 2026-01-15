package com.nks.hms.controller;

import com.nks.hms.model.PatientFeedback;
import com.nks.hms.service.IPatientFeedbackService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for patient feedback UI operations.
 * Separates UI logic from Main class (Single Responsibility Principle).
 * Depends on IPatientFeedbackService abstraction (Dependency Inversion Principle).
 */
public class PatientFeedbackController {
    private static final int PAGE_SIZE = 25;
    private final IPatientFeedbackService feedbackService;
    
    public PatientFeedbackController(IPatientFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }
    
    /**
     * Loads feedback into table with pagination and sorting.
     */
    public void loadFeedback(TableView<PatientFeedback> table, Pagination pagination, 
                            String searchTerm, String sortBy, Label feedback) {
        try {
            int total = feedbackService.countFeedback(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            
            List<PatientFeedback> feedbackList = feedbackService.searchFeedback(searchTerm, PAGE_SIZE, offset, sortBy);
            table.setItems(FXCollections.observableArrayList(feedbackList));
            
            feedback.setText(total + " feedback records found");
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load feedback: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Saves or updates a feedback.
     */
    public void saveFeedback(TableView<PatientFeedback> table, Pagination pagination, 
                            TextField searchField, ComboBox<String> sortCombo, Label feedbackLabel,
                            ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                            ComboBox<Integer> ratingCombo, TextArea commentsArea) {
        PatientFeedback selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        
        if (isNew) {
            selected = new PatientFeedback();
        }
        
        // Validate selections
        if (patientCombo.getValue() == null) {
            feedbackLabel.setText("Please select a patient");
            feedbackLabel.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        if (doctorCombo.getValue() == null) {
            feedbackLabel.setText("Please select a doctor");
            feedbackLabel.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        selected.setPatientId(patientCombo.getValue());
        selected.setDoctorId(doctorCombo.getValue());
        selected.setRating(ratingCombo.getValue());
        selected.setComments(commentsArea.getText());
        selected.setFeedbackDate(LocalDateTime.now());
        
        try {
            int id = feedbackService.saveFeedback(selected);
            selected.setId(id);
            feedbackLabel.setText(isNew ? "Feedback saved" : "Feedback updated");
            feedbackLabel.setStyle("-fx-text-fill: #006400;");
            loadFeedback(table, pagination, searchField.getText(), sortCombo.getValue(), feedbackLabel);
            clearForm(patientCombo, doctorCombo, ratingCombo, commentsArea);
            table.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            feedbackLabel.setText(ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #B00020;");
        } catch (SQLException ex) {
            feedbackLabel.setText("Failed to save feedback: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Deletes a feedback.
     */
    public void deleteFeedback(TableView<PatientFeedback> table, Pagination pagination,
                              TextField searchField, ComboBox<String> sortCombo, Label feedbackLabel,
                              ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                              ComboBox<Integer> ratingCombo, TextArea commentsArea) {
        PatientFeedback selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedbackLabel.setText("Select a feedback to delete");
            feedbackLabel.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            feedbackService.deleteFeedback(selected.getId());
            feedbackLabel.setText("Feedback deleted");
            feedbackLabel.setStyle("-fx-text-fill: #006400;");
            loadFeedback(table, pagination, searchField.getText(), sortCombo.getValue(), feedbackLabel);
            clearForm(patientCombo, doctorCombo, ratingCombo, commentsArea);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedbackLabel.setText("Failed to delete feedback: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Populates form from selected feedback.
     */
    public void populateForm(PatientFeedback feedback, ComboBox<Integer> patientCombo,
                            ComboBox<Integer> doctorCombo, ComboBox<Integer> ratingCombo,
                            TextArea commentsArea) {
        if (feedback == null) {
            clearForm(patientCombo, doctorCombo, ratingCombo, commentsArea);
            return;
        }
        patientCombo.setValue(feedback.getPatientId());
        doctorCombo.setValue(feedback.getDoctorId());
        ratingCombo.setValue(feedback.getRating());
        commentsArea.setText(feedback.getComments());
    }
    
    /**
     * Clears form fields.
     */
    public void clearForm(ComboBox<Integer> patientCombo, ComboBox<Integer> doctorCombo,
                         ComboBox<Integer> ratingCombo, TextArea commentsArea) {
        patientCombo.setValue(null);
        doctorCombo.setValue(null);
        ratingCombo.setValue(null);
        commentsArea.clear();
    }
}
