package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
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
 * Repository layer for patient data access using JDBC.
 * 
 * <p>Provides CRUD operations and search functionality for patient records.
 * All methods execute direct SQL queries against the 'patient' table in the MySQL database.
 * Search operations support case-insensitive filtering across multiple fields
 * (first name, last name, phone, email).
 * 
 * <p>Key features:
 * <ul>
 *   <li>Paginated search with configurable limit and offset</li>
 *   <li>Case-insensitive LIKE queries for flexible searching</li>
 *   <li>Visit history retrieval via JOIN with appointment and doctor tables</li>
 *   <li>Parameterized queries to prevent SQL injection</li>
 * </ul>
 * 
 * <p>All database operations use try-with-resources to ensure proper connection cleanup.
 * Methods throw SQLException which should be handled by the caller (typically the UI layer).
 * 
 * @see com.nks.hms.model.Patient
 * @see com.nks.hms.db.Database
 */
// Thin JDBC repository with basic search, pagination, and history lookups.
public class PatientRepository implements IPatientRepository {
    private static final String BASE_SELECT = "SELECT ID, FirstName, MiddleName, LastName, Email, PhoneNumber, DateOfBirth, Address FROM patient";

    /**
     * Searches for patients matching the given search term with pagination support.
     * Optimized to perform direct ID lookup if search term is a valid integer.
     * 
     * <p>The search is case-insensitive and matches against first name, last name,
     * phone number, and email address. Results are ordered by ID descending (newest first).
     * 
     * @param searchTerm Text to search for (can be null or empty to return all patients)
     * @param limit Maximum number of records to return (page size)
     * @param offset Number of records to skip (for pagination)
     * @return List of matching patients (empty list if no matches)
     * @throws SQLException If database query fails
     */
    // Case-insensitive search across name/phone/email; ordered by newest first.
    // Optimized: if searchTerm is numeric, performs direct ID lookup.
    @Override
    public List<Patient> find(String searchTerm, int limit, int offset) throws SQLException {
        // Fast path: if search term is a pure integer, do direct ID lookup
        if (searchTerm != null && !searchTerm.isBlank()) {
            try {
                int id = Integer.parseInt(searchTerm.trim());
                Optional<Patient> patient = findById(id);
                return patient.map(List::of).orElse(List.of());
            } catch (NumberFormatException e) {
                // Not a number, fall through to text search
            }
        }
        
        return findByText(searchTerm, limit, offset);
    }

    /**
     * Text-based search for patients (internal method).
     * Performs case-insensitive LIKE queries across multiple fields.
     * 
     * @param searchTerm Text pattern to search for
     * @param limit Maximum records to return
     * @param offset Records to skip
     * @return List of matching patients
     * @throws SQLException If query fails
     */
    private List<Patient> findByText(String searchTerm, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<String> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE FirstName LIKE ? OR LastName LIKE ? OR PhoneNumber LIKE ? OR Email LIKE ?");
            String pattern = "%" + searchTerm.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        sql.append(" ORDER BY ID DESC LIMIT ? OFFSET ?");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (String param : params) {
                stmt.setString(index++, param);
            }
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            List<Patient> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPatient(rs));
                }
            }
            return results;
        }
    }

    /**
     * Counts the total number of patients matching the given search term.
     * Uses the same filtering logic as find() to ensure consistency.
     * Optimized to return 0 or 1 for numeric ID searches.
     * 
     * @param searchTerm Text to search for (can be null or empty to count all)
     * @return Total number of matching patient records
     * @throws SQLException If database query fails
     */
    // Count rows matching the same filters used in find().
    // Optimized: for numeric searches, returns 0 or 1 quickly.
    @Override
    public int count(String searchTerm) throws SQLException {
        // Fast path: if search term is numeric, check if that ID exists
        if (searchTerm != null && !searchTerm.isBlank()) {
            try {
                int id = Integer.parseInt(searchTerm.trim());
                return findById(id).isPresent() ? 1 : 0;
            } catch (NumberFormatException e) {
                // Not a number, fall through to text count
            }
        }
        
        return countByText(searchTerm);
    }

    /**
     * Text-based count (internal method).
     * Counts records matching the text pattern.
     * 
     * @param searchTerm Text pattern to count
     * @return Number of matching records
     * @throws SQLException If query fails
     */
    private int countByText(String searchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM patient");
        List<String> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE FirstName LIKE ? OR LastName LIKE ? OR PhoneNumber LIKE ? OR Email LIKE ?");
            String pattern = "%" + searchTerm.trim() + "%";
            params.add(pattern);
            params.add(pattern);
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
     * Retrieves a single patient by their unique database ID.
     * 
     * @param id The patient's primary key
     * @return Optional containing the patient if found, empty otherwise
     * @throws SQLException If database query fails
     */
    // Fetch a single patient by ID.
    @Override
    public Optional<Patient> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPatient(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new patient record and returns the auto-generated ID.
     * The patient's ID field is ignored as the database generates it.
     * 
     * @param patient Patient object with required fields populated
     * @return The auto-generated database ID, or -1 if insert failed
     * @throws SQLException If insert fails
     */
    // Insert a new patient and return generated key.
    @Override
    public int insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patient (FirstName, MiddleName, LastName, Email, PhoneNumber, DateOfBirth, Address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindPatient(stmt, patient);
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
     * Updates an existing patient record with new values.
     * The patient's ID must be set and match an existing record.
     * 
     * @param patient Patient object with ID and updated fields
     * @throws SQLException If update fails or ID doesn't exist
     */
    // Update an existing patient record.
    @Override
    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE patient SET FirstName = ?, MiddleName = ?, LastName = ?, Email = ?, PhoneNumber = ?, DateOfBirth = ?, Address = ? WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPatient(stmt, patient);
            stmt.setInt(8, patient.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Permanently deletes a patient record from the database.
     * Warning: Hard delete with no archive mechanism.
     * 
     * @param id The ID of the patient to delete
     * @throws SQLException If delete fails (e.g., foreign key constraint)
     */
    // Delete a patient by ID.
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM patient WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves appointment history for a patient via JOIN query.
     * Results are ordered by appointment date descending (most recent first).
     * 
     * @param patientId The ID of the patient
     * @return List of visit history records (empty if none found)
     * @throws SQLException If query fails
     */
    // Retrieve recent appointment history for display-only purposes.
    @Override
    public List<VisitHistory> getVisitHistory(int patientId) throws SQLException {
        String sql = "SELECT d.FirstName, d.LastName, a.AppointmentDate, a.Reason, '' as Notes "
                + "FROM appointment a "
                + "JOIN doctor d ON a.DoctorID = d.ID "
                + "WHERE a.PatientID = ? "
                + "ORDER BY a.AppointmentDate DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            List<VisitHistory> visits = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String doctorName = rs.getString(1) + " " + rs.getString(2);
                    Date visit = rs.getDate(3);
                    LocalDate visitDate = visit != null ? visit.toLocalDate() : null;
                    String reason = rs.getString(4);
                    String notes = rs.getString(5);
                    visits.add(new VisitHistory(doctorName, visitDate, reason, notes));
                }
            }
            return visits;
        }
    }

    /**
     * Binds patient field values to a prepared statement.
     * 
     * <p>Helper method used by insert and update operations to set all patient
     * fields in the correct order. Handles null date conversion properly.
     * 
     * @param stmt The prepared statement to bind parameters to
     * @param patient The patient whose data should be bound
     * @throws SQLException If parameter binding fails
     */
    private void bindPatient(PreparedStatement stmt, Patient patient) throws SQLException {
        stmt.setString(1, patient.getFirstName());
        stmt.setString(2, patient.getMiddleName());
        stmt.setString(3, patient.getLastName());
        stmt.setString(4, patient.getEmail());
        stmt.setString(5, patient.getPhone());
        if (patient.getDateOfBirth() != null) {
            stmt.setDate(6, Date.valueOf(patient.getDateOfBirth()));
        } else {
            stmt.setNull(6, java.sql.Types.DATE);
        }
        stmt.setString(7, patient.getAddress());
    }

    /**
     * Maps a database result set row to a Patient object.
     * 
     * <p>Helper method that extracts all patient fields from the current row
     * of a result set and constructs a Patient instance. Handles null values
     * and type conversions (SQL Date to LocalDate).
     * 
     * @param rs The result set positioned at the row to map
     * @return A Patient object populated with data from the result set
     * @throws SQLException If column access fails or columns are missing
     */
    private Patient mapPatient(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        String first = rs.getString("FirstName");
        String middle = rs.getString("MiddleName");
        String last = rs.getString("LastName");
        String email = rs.getString("Email");
        String phone = rs.getString("PhoneNumber");
        Date dob = rs.getDate("DateOfBirth");
        String address = rs.getString("Address");
        LocalDate dateOfBirth = dob != null ? dob.toLocalDate() : null;
        return new Patient(id, first, middle, last, dateOfBirth, phone, email, address);
    }
}
