package com.nks.hms.service;

import com.nks.hms.model.Prescription;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for prescription business operations.
 * Abstracts business logic away from UI and repository layers.
 * Follows dependency inversion principle.
 */
public interface IPrescriptionService {
    /**
     * Searches for prescriptions with pagination and sorting.
     * 
     * @param searchTerm Search text (null/empty for all)
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching prescriptions
     * @throws SQLException If database access fails
     */
    List<Prescription> searchPrescriptions(String searchTerm, int limit, int offset, String sortBy) throws SQLException;
    
    /**
     * Counts total prescriptions matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count
     * @throws SQLException If database access fails
     */
    int countPrescriptions(String searchTerm) throws SQLException;
    
    /**
     * Finds a prescription by ID.
     * 
     * @param id Prescription ID
     * @return Optional containing prescription if found
     * @throws SQLException If database access fails
     */
    Optional<Prescription> getPrescriptionById(int id) throws SQLException;
    
    /**
     * Saves a new prescription or updates an existing one.
     * 
     * @param prescription Prescription to save
     * @return Generated ID for new prescriptions, existing ID for updates
     * @throws SQLException If save fails
     */
    int savePrescription(Prescription prescription) throws SQLException;
    
    /**
     * Deletes a prescription by ID.
     * 
     * @param id Prescription ID to delete
     * @throws SQLException If deletion fails
     */
    void deletePrescription(int id) throws SQLException;
    
    /**
     * Gets cache statistics for monitoring.
     * 
     * @return Cache statistics string
     */
    String getCacheStats();
}
