package com.nks.hms.service;

import com.nks.hms.model.Doctor;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for doctor business operations.
 * Abstracts business logic away from UI and repository layers.
 * Follows dependency inversion principle.
 */
public interface IDoctorService {
    /**
     * Searches for doctors with pagination and sorting.
     * 
     * @param searchTerm Search text (null/empty for all)
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching doctors
     * @throws SQLException If database access fails
     */
    List<Doctor> searchDoctors(String searchTerm, int limit, int offset, String sortBy) throws SQLException;
    
    /**
     * Counts total doctors matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count
     * @throws SQLException If database access fails
     */
    int countDoctors(String searchTerm) throws SQLException;
    
    /**
     * Finds a doctor by ID.
     * 
     * @param id Doctor ID
     * @return Optional containing doctor if found
     * @throws SQLException If database access fails
     */
    Optional<Doctor> getDoctorById(int id) throws SQLException;
    
    /**
     * Saves a new doctor or updates an existing one.
     * 
     * @param doctor Doctor to save
     * @return Generated ID for new doctors, existing ID for updates
     * @throws SQLException If save fails
     */
    int saveDoctor(Doctor doctor) throws SQLException;
    
    /**
     * Deletes a doctor by ID.
     * 
     * @param id Doctor ID to delete
     * @throws SQLException If deletion fails
     */
    void deleteDoctor(int id) throws SQLException;
    
    /**
     * Gets cache statistics for monitoring.
     * 
     * @return Cache statistics string
     */
    String getCacheStats();
}
