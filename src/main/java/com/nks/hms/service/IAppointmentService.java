package com.nks.hms.service;

import com.nks.hms.model.Appointment;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for appointment business operations.
 * Abstracts business logic away from UI and repository layers.
 * Follows dependency inversion principle.
 */
public interface IAppointmentService {
    /**
     * Searches for appointments with pagination and sorting.
     * 
     * @param searchTerm Search text (null/empty for all)
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching appointments
     * @throws SQLException If database access fails
     */
    List<Appointment> searchAppointments(String searchTerm, int limit, int offset, String sortBy) throws SQLException;
    
    /**
     * Counts total appointments matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count of matching records
     * @throws SQLException If database access fails
     */
    int countAppointments(String searchTerm, String sortBy) throws SQLException;
    
    /**
     * Retrieves an appointment by ID.
     * 
     * @param id The appointment ID
     * @return Optional containing appointment if found
     * @throws SQLException If database access fails
     */
    Optional<Appointment> getAppointmentById(int id) throws SQLException;
    
    /**
     * Saves or creates a new appointment.
     * 
     * @param appointment The appointment to save
     * @return Generated ID if new, or existing ID if updated
     * @throws SQLException If database operation fails
     * @throws IllegalArgumentException If validation fails
     */
    int saveAppointment(Appointment appointment) throws SQLException;
    
    /**
     * Updates an existing appointment.
     * 
     * @param appointment The appointment with updated data
     * @throws SQLException If database operation fails
     * @throws IllegalArgumentException If validation fails
     */
    void updateAppointment(Appointment appointment) throws SQLException;
    
    /**
     * Deletes an appointment.
     * 
     * @param id The appointment ID to delete
     * @throws SQLException If database operation fails
     */
    void deleteAppointment(int id) throws SQLException;
    
    /**
     * Retrieves all appointments for a specific patient.
     * 
     * @param patientId The patient's ID
     * @return List of appointments
     * @throws SQLException If database access fails
     */
    List<Appointment> getAppointmentsByPatient(int patientId) throws SQLException;
    
    /**
     * Retrieves all appointments for a specific doctor.
     * 
     * @param doctorId The doctor's ID
     * @return List of appointments
     * @throws SQLException If database access fails
     */
    List<Appointment> getAppointmentsByDoctor(int doctorId) throws SQLException;
    
    /**
     * Gets cache statistics for display.
     * 
     * @return String with cache info
     */
    String getCacheStats();
}
