package com.nks.hms.repository;

import com.nks.hms.model.Appointment;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for appointment data access operations.
 * Defines contract for CRUD operations on appointments.
 */
public interface IAppointmentRepository {
    /**
     * Searches for appointments with pagination and optional sorting.
     * 
     * @param searchTerm Search text (patient name, doctor name, or reason)
     * @param limit Maximum number of records to return
     * @param offset Number of records to skip
     * @return List of matching appointments
     * @throws SQLException If database query fails
     */
    List<Appointment> find(String searchTerm, int limit, int offset) throws SQLException;
    
    /**
     * Counts total appointments matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count of matching records
     * @throws SQLException If database query fails
     */
    int count(String searchTerm) throws SQLException;
    
    /**
     * Retrieves an appointment by ID.
     * 
     * @param id Primary key of the appointment
     * @return Optional containing appointment if found
     * @throws SQLException If database query fails
     */
    Optional<Appointment> findById(int id) throws SQLException;
    
    /**
     * Creates a new appointment.
     * 
     * @param appointment The appointment to create
     * @return Generated ID of the new appointment
     * @throws SQLException If database operation fails
     */
    int create(Appointment appointment) throws SQLException;
    
    /**
     * Updates an existing appointment.
     * 
     * @param appointment The appointment with updated data
     * @throws SQLException If database operation fails
     */
    void update(Appointment appointment) throws SQLException;
    
    /**
     * Deletes an appointment by ID.
     * 
     * @param id Primary key of the appointment to delete
     * @throws SQLException If database operation fails
     */
    void delete(int id) throws SQLException;
    
    /**
     * Retrieves all appointments for a specific patient.
     * 
     * @param patientId The patient's ID
     * @return List of appointments for the patient
     * @throws SQLException If database query fails
     */
    List<Appointment> findByPatientId(int patientId) throws SQLException;
    
    /**
     * Retrieves all appointments for a specific doctor.
     * 
     * @param doctorId The doctor's ID
     * @return List of appointments for the doctor
     * @throws SQLException If database query fails
     */
    List<Appointment> findByDoctorId(int doctorId) throws SQLException;
}
