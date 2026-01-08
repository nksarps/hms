package com.nks.hms.service;

import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for patient business operations.
 * Abstracts business logic away from UI and repository layers.
 * Follows dependency inversion principle.
 */
public interface IPatientService {
    /**
     * Searches for patients with pagination and sorting.
     * 
     * @param searchTerm Search text (null/empty for all)
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching patients
     * @throws SQLException If database access fails
     */
    List<Patient> searchPatients(String searchTerm, int limit, int offset, String sortBy) throws SQLException;
    
    /**
     * Counts total patients matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count
     * @throws SQLException If database access fails
     */
    int countPatients(String searchTerm) throws SQLException;
    
    /**
     * Finds a patient by ID.
     * 
     * @param id Patient ID
     * @return Optional containing patient if found
     * @throws SQLException If database access fails
     */
    Optional<Patient> getPatientById(int id) throws SQLException;
    
    /**
     * Retrieves visit history for a patient.
     * 
     * @param patientId Patient ID
     * @return List of visit history records
     * @throws SQLException If database access fails
     */
    List<VisitHistory> getVisitHistory(int patientId) throws SQLException;
    
    /**
     * Saves a new patient or updates an existing one.
     * 
     * @param patient Patient to save
     * @return Generated ID for new patients, existing ID for updates
     * @throws SQLException If save fails
     */
    int savePatient(Patient patient) throws SQLException;
    
    /**
     * Deletes a patient by ID.
     * 
     * @param id Patient ID to delete
     * @throws SQLException If deletion fails
     */
    void deletePatient(int id) throws SQLException;
    
    /**
     * Gets cache statistics for monitoring.
     * 
     * @return Cache statistics string
     */
    String getCacheStats();
}
