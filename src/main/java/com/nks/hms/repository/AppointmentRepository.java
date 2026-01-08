package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository layer for appointment data access using JDBC.
 * 
 * <p>Provides CRUD operations and search functionality for appointment records.
 * All methods execute direct SQL queries against the 'appointment' table in the MySQL database.
 * Search operations support case-insensitive filtering across patient names, doctor names, and reason.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Paginated search with configurable limit and offset</li>
 *   <li>Case-insensitive LIKE queries for flexible searching</li>
 *   <li>Joins with patient and doctor tables to include display names</li>
 *   <li>Parameterized queries to prevent SQL injection</li>
 * </ul>
 * 
 * <p>All database operations use try-with-resources to ensure proper connection cleanup.
 * Methods throw SQLException which should be handled by the caller (typically the UI layer).
 * 
 * @see com.nks.hms.model.Appointment
 * @see com.nks.hms.db.Database
 */
public class AppointmentRepository implements IAppointmentRepository {
    private static final String BASE_SELECT = 
        "SELECT a.ID, a.PatientID, a.DoctorID, a.AppointmentDate, a.Reason, " +
        "CONCAT(p.FirstName, ' ', COALESCE(p.MiddleName, ''), ' ', p.LastName) as PatientName, " +
        "CONCAT(d.FirstName, ' ', COALESCE(d.MiddleName, ''), ' ', d.LastName) as DoctorName " +
        "FROM appointment a " +
        "JOIN patient p ON a.PatientID = p.ID " +
        "JOIN doctor d ON a.DoctorID = d.ID";

    /**
     * Searches for appointments matching the given search term with pagination support.
     * 
     * <p>The search is case-insensitive and matches against patient name, doctor name,
     * and reason. Results are ordered by appointment date descending (newest first).
     * 
     * @param searchTerm Text to search for (can be null or empty to return all appointments)
     * @param limit Maximum number of records to return (page size)
     * @param offset Number of records to skip (for pagination)
     * @return List of matching appointments (empty list if no matches)
     * @throws SQLException If database query fails
     */
    @Override
    public List<Appointment> find(String searchTerm, int limit, int offset) throws SQLException {
        StringBuilder query = new StringBuilder(BASE_SELECT);
        
        if (searchTerm != null && !searchTerm.isBlank()) {
            query.append(" WHERE LOWER(CONCAT(p.FirstName, ' ', COALESCE(p.MiddleName, ''), ' ', p.LastName)) LIKE LOWER(?) " +
                        "OR LOWER(CONCAT(d.FirstName, ' ', COALESCE(d.MiddleName, ''), ' ', d.LastName)) LIKE LOWER(?) " +
                        "OR LOWER(a.Reason) LIKE LOWER(?)");
        }
        
        query.append(" ORDER BY a.AppointmentDate DESC LIMIT ? OFFSET ?");
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            
            int paramIndex = 1;
            if (searchTerm != null && !searchTerm.isBlank()) {
                String searchPattern = "%" + searchTerm + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);
            
            List<Appointment> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultToAppointment(rs));
                }
            }
            return results;
        }
    }

    /**
     * Counts total appointments matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count of matching records
     * @throws SQLException If database query fails
     */
    @Override
    public int count(String searchTerm) throws SQLException {
        StringBuilder query = new StringBuilder(
            "SELECT COUNT(*) FROM appointment a " +
            "JOIN patient p ON a.PatientID = p.ID " +
            "JOIN doctor d ON a.DoctorID = d.ID"
        );
        
        if (searchTerm != null && !searchTerm.isBlank()) {
            query.append(" WHERE LOWER(CONCAT(p.FirstName, ' ', COALESCE(p.MiddleName, ''), ' ', p.LastName)) LIKE LOWER(?) " +
                        "OR LOWER(CONCAT(d.FirstName, ' ', COALESCE(d.MiddleName, ''), ' ', d.LastName)) LIKE LOWER(?) " +
                        "OR LOWER(a.Reason) LIKE LOWER(?)");
        }
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            
            if (searchTerm != null && !searchTerm.isBlank()) {
                String searchPattern = "%" + searchTerm + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Retrieves an appointment by ID.
     * 
     * @param id Primary key of the appointment
     * @return Optional containing appointment if found
     * @throws SQLException If database query fails
     */
    @Override
    public Optional<Appointment> findById(int id) throws SQLException {
        String query = BASE_SELECT + " WHERE a.ID = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultToAppointment(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Creates a new appointment.
     * 
     * @param appointment The appointment to create
     * @return Generated ID of the new appointment
     * @throws SQLException If database operation fails
     */
    @Override
    public int create(Appointment appointment) throws SQLException {
        String query = "INSERT INTO appointment (PatientID, DoctorID, AppointmentDate, Reason) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDate()));
            stmt.setString(4, appointment.getReason());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating appointment failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating appointment failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing appointment.
     * 
     * @param appointment The appointment with updated data
     * @throws SQLException If database operation fails
     */
    @Override
    public void update(Appointment appointment) throws SQLException {
        String query = "UPDATE appointment SET PatientID = ?, DoctorID = ?, AppointmentDate = ?, Reason = ? " +
                      "WHERE ID = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDate()));
            stmt.setString(4, appointment.getReason());
            stmt.setInt(5, appointment.getId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an appointment by ID.
     * 
     * @param id Primary key of the appointment to delete
     * @throws SQLException If database operation fails
     */
    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM appointment WHERE ID = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves all appointments for a specific patient.
     * 
     * @param patientId The patient's ID
     * @return List of appointments for the patient
     * @throws SQLException If database query fails
     */
    @Override
    public List<Appointment> findByPatientId(int patientId) throws SQLException {
        String query = BASE_SELECT + " WHERE a.PatientID = ? ORDER BY a.AppointmentDate DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, patientId);
            List<Appointment> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultToAppointment(rs));
                }
            }
            return results;
        }
    }

    /**
     * Retrieves all appointments for a specific doctor.
     * 
     * @param doctorId The doctor's ID
     * @return List of appointments for the doctor
     * @throws SQLException If database query fails
     */
    @Override
    public List<Appointment> findByDoctorId(int doctorId) throws SQLException {
        String query = BASE_SELECT + " WHERE a.DoctorID = ? ORDER BY a.AppointmentDate DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, doctorId);
            List<Appointment> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultToAppointment(rs));
                }
            }
            return results;
        }
    }

    /**
     * Maps a database ResultSet row to an Appointment object.
     * 
     * @param rs The ResultSet positioned at the row to map
     * @return Populated Appointment object
     * @throws SQLException If column retrieval fails
     */
    private Appointment mapResultToAppointment(ResultSet rs) throws SQLException {
        LocalDateTime appointmentDate = rs.getTimestamp("AppointmentDate").toLocalDateTime();
        
        return new Appointment(
            rs.getInt("ID"),
            rs.getInt("PatientID"),
            rs.getInt("DoctorID"),
            appointmentDate,
            rs.getString("Reason"),
            rs.getString("PatientName"),
            rs.getString("DoctorName")
        );
    }
}
