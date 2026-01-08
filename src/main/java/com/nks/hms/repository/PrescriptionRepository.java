package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.Prescription;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository layer for prescription data access using JDBC.
 * 
 * <p>Provides CRUD operations and search functionality for prescription records.
 * All methods execute direct SQL queries against the 'Prescription' table in the MySQL database.
 * Search operations support case-insensitive filtering across patient names and doctor names.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Paginated search with configurable limit and offset</li>
 *   <li>Case-insensitive LIKE queries for flexible searching</li>
 *   <li>JOIN with patient and doctor tables to get names</li>
 *   <li>Parameterized queries to prevent SQL injection</li>
 * </ul>
 * 
 * <p>All database operations use try-with-resources to ensure proper connection cleanup.
 * Methods throw SQLException which should be handled by the caller (typically the UI layer).
 * 
 * @see com.nks.hms.model.Prescription
 * @see com.nks.hms.db.Database
 */
public class PrescriptionRepository implements IPrescriptionRepository {
    private static final String BASE_SELECT = "SELECT p.ID, p.PatientID, p.DoctorID, " +
            "CONCAT(pat.FirstName, ' ', pat.LastName) as PatientName, " +
            "CONCAT(d.FirstName, ' ', d.LastName) as DoctorName, " +
            "p.PrescriptionDate, p.Notes " +
            "FROM Prescription p " +
            "LEFT JOIN Patient pat ON p.PatientID = pat.ID " +
            "LEFT JOIN Doctor d ON p.DoctorID = d.ID";

    /**
     * Searches for prescriptions matching the given search term with pagination support.
     * 
     * <p>The search is case-insensitive and matches against patient name and doctor name.
     * Results are ordered by prescription date descending (newest first).
     * 
     * @param searchTerm Text to search for (can be null or empty to return all prescriptions)
     * @param limit Maximum number of records to return (page size)
     * @param offset Number of records to skip (for pagination)
     * @return List of matching prescriptions (empty list if no matches)
     * @throws SQLException If database query fails
     */
    @Override
    public List<Prescription> find(String searchTerm, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<String> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE CONCAT(pat.FirstName, ' ', pat.LastName) LIKE ? OR CONCAT(d.FirstName, ' ', d.LastName) LIKE ?");
            String pattern = "%" + searchTerm.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        
        sql.append(" ORDER BY p.PrescriptionDate DESC LIMIT ? OFFSET ?");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (String param : params) {
                stmt.setString(index++, param);
            }
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            List<Prescription> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPrescription(rs));
                }
            }
            return results;
        }
    }

    /**
     * Counts the total number of prescriptions matching the given search term.
     * Uses the same filtering logic as find() to ensure consistency.
     * 
     * @param searchTerm Text to search for (can be null or empty to count all)
     * @return Total number of matching prescription records
     * @throws SQLException If database query fails
     */
    @Override
    public int count(String searchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Prescription p LEFT JOIN Patient pat ON p.PatientID = pat.ID LEFT JOIN Doctor d ON p.DoctorID = d.ID");
        List<String> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE CONCAT(pat.FirstName, ' ', pat.LastName) LIKE ? OR CONCAT(d.FirstName, ' ', d.LastName) LIKE ?");
            String pattern = "%" + searchTerm.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (String param : params) {
                stmt.setString(index++, param);
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
     * Retrieves a single prescription by its unique database ID.
     * 
     * @param id The prescription's primary key
     * @return Optional containing the prescription if found, empty otherwise
     * @throws SQLException If database query fails
     */
    @Override
    public Optional<Prescription> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE p.ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPrescription(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new prescription record and returns the auto-generated ID.
     * The prescription's ID field is ignored as the database generates it.
     * 
     * @param prescription Prescription object with required fields populated
     * @return The auto-generated database ID, or -1 if insert failed
     * @throws SQLException If insert fails
     */
    @Override
    public int insert(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO Prescription (PatientID, DoctorID, PrescriptionDate, Notes) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindPrescription(stmt, prescription);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing prescription record with new values.
     * The prescription's ID must be set and match an existing record.
     * 
     * @param prescription Prescription object with ID and updated fields
     * @throws SQLException If update fails or ID doesn't exist
     */
    @Override
    public void update(Prescription prescription) throws SQLException {
        String sql = "UPDATE Prescription SET PatientID = ?, DoctorID = ?, PrescriptionDate = ?, Notes = ? WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPrescription(stmt, prescription);
            stmt.setInt(5, prescription.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Permanently deletes a prescription record from the database.
     * Warning: Hard delete with no archive mechanism.
     * 
     * @param id The ID of the prescription to delete
     * @throws SQLException If delete fails
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Prescription WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Binds prescription field values to a prepared statement.
     * 
     * <p>Helper method used by insert and update operations to set all prescription
     * fields in the correct order. Handles null date conversion properly.
     * 
     * @param stmt The prepared statement to bind parameters to
     * @param prescription The prescription whose data should be bound
     * @throws SQLException If parameter binding fails
     */
    private void bindPrescription(PreparedStatement stmt, Prescription prescription) throws SQLException {
        stmt.setInt(1, prescription.getPatientId());
        stmt.setInt(2, prescription.getDoctorId());
        
        if (prescription.getPrescriptionDate() != null) {
            stmt.setDate(3, Date.valueOf(prescription.getPrescriptionDate()));
        } else {
            stmt.setNull(3, java.sql.Types.DATE);
        }
        
        stmt.setString(4, prescription.getNotes());
    }

    /**
     * Maps a database result set row to a Prescription object.
     * 
     * <p>Helper method that extracts all prescription fields from the current row
     * of a result set and constructs a Prescription instance. Handles null values
     * and type conversions (SQL Date to LocalDate).
     * 
     * @param rs The result set positioned at the row to map
     * @return A Prescription object populated with data from the result set
     * @throws SQLException If column access fails or columns are missing
     */
    private Prescription mapPrescription(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        int patientId = rs.getInt("PatientID");
        int doctorId = rs.getInt("DoctorID");
        String patientName = rs.getString("PatientName");
        String doctorName = rs.getString("DoctorName");
        
        Date prescriptionDateSql = rs.getDate("PrescriptionDate");
        LocalDate prescriptionDate = prescriptionDateSql != null ? prescriptionDateSql.toLocalDate() : null;
        
        String notes = rs.getString("Notes");
        
        return new Prescription(id, patientId, doctorId, patientName, doctorName, prescriptionDate, notes);
    }
}
