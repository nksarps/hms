package com.nks.hms.service;

import com.nks.hms.model.PatientFeedback;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for patient feedback business operations.
 * Abstracts business logic away from UI and repository layers.
 * Follows dependency inversion principle.
 */
public interface IPatientFeedbackService {
    /**
     * Searches for patient feedback with pagination and sorting.
     * 
     * @param searchTerm Search text (null/empty for all)
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching feedback
     * @throws SQLException If database access fails
     */
    List<PatientFeedback> searchFeedback(String searchTerm, int limit, int offset, String sortBy) throws SQLException;
    
    /**
     * Counts total feedback matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count
     * @throws SQLException If database access fails
     */
    int countFeedback(String searchTerm) throws SQLException;
    
    /**
     * Finds a feedback by ID.
     * 
     * @param id Feedback ID
     * @return Optional containing feedback if found
     * @throws SQLException If database access fails
     */
    Optional<PatientFeedback> getFeedbackById(int id) throws SQLException;
    
    /**
     * Saves a new feedback or updates an existing one.
     * 
     * @param feedback Feedback to save
     * @return Generated ID for new feedback, existing ID for updates
     * @throws SQLException If save fails
     */
    int saveFeedback(PatientFeedback feedback) throws SQLException;
    
    /**
     * Deletes a feedback by ID.
     * 
     * @param id Feedback ID to delete
     * @throws SQLException If deletion fails
     */
    void deleteFeedback(int id) throws SQLException;
    
    /**
     * Gets cache statistics for monitoring.
     * 
     * @return Cache statistics string
     */
    String getCacheStats();
}
