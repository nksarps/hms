package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.PatientFeedback;
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
 * Repository layer for patient feedback data access using JDBC.
 * 
 * <p>Provides CRUD operations and search functionality for patient feedback records.
 * All methods execute direct SQL queries against the 'PatientFeedback' table in the MySQL database.
 * Search operations support case-insensitive filtering across patient and doctor names.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Paginated search with configurable limit and offset</li>
 *   <li>JOIN queries with Patient and Doctor tables for display names</li>
 *   <li>Parameterized queries to prevent SQL injection</li>
 * </ul>
 * 
 * <p>All database operations use try-with-resources to ensure proper connection cleanup.
 * Methods throw SQLException which should be handled by the caller (typically the UI layer).
 * 
 * @see com.nks.hms.model.PatientFeedback
 * @see com.nks.hms.db.Database
 */
public class PatientFeedbackRepository implements IPatientFeedbackRepository {
    private static final String BASE_SELECT = 
        "SELECT pf.ID, pf.PatientID, pf.DoctorID, pf.Rating, pf.Comments, pf.FeedbackDate, " +
        "CONCAT(p.FirstName, ' ', p.LastName) as PatientName, " +
        "CONCAT(d.FirstName, ' ', d.LastName) as DoctorName " +
        "FROM PatientFeedback pf " +
        "JOIN Patient p ON pf.PatientID = p.ID " +
        "JOIN Doctor d ON pf.DoctorID = d.ID";

    /**
     * Searches for patient feedback matching the given search term with pagination support.
     * 
     * <p>The search is case-insensitive and matches against patient name and doctor name.
     * Results are ordered by feedback date descending (most recent first).
     * 
     * @param searchTerm Text to search for (can be null or empty to return all feedback)
     * @param limit Maximum number of records to return (page size)
     * @param offset Number of records to skip (for pagination)
     * @return List of matching patient feedback (empty list if no matches)
     * @throws SQLException If database query fails
     */
    @Override
    public List<PatientFeedback> find(String searchTerm, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<String> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE CONCAT(p.FirstName, ' ', p.LastName) LIKE ? OR CONCAT(d.FirstName, ' ', d.LastName) LIKE ?");
            String pattern = "%" + searchTerm.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        sql.append(" ORDER BY pf.FeedbackDate DESC LIMIT ? OFFSET ?");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (String param : params) {
                stmt.setString(index++, param);
            }
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            List<PatientFeedback> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPatientFeedback(rs));
                }
            }
            return results;
        }
    }

    /**
     * Counts the total number of patient feedback records matching the given search term.
     * Uses the same filtering logic as find() to ensure consistency.
     * 
     * @param searchTerm Text to search for (can be null or empty to count all)
     * @return Total number of matching feedback records
     * @throws SQLException If database query fails
     */
    @Override
    public int count(String searchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM PatientFeedback pf " +
            "JOIN Patient p ON pf.PatientID = p.ID " +
            "JOIN Doctor d ON pf.DoctorID = d.ID");
        List<String> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE CONCAT(p.FirstName, ' ', p.LastName) LIKE ? OR CONCAT(d.FirstName, ' ', d.LastName) LIKE ?");
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
     * Retrieves a single patient feedback by its unique database ID.
     * 
     * @param id The feedback's primary key
     * @return Optional containing the feedback if found, empty otherwise
     * @throws SQLException If database query fails
     */
    @Override
    public Optional<PatientFeedback> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE pf.ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPatientFeedback(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new patient feedback and returns the auto-generated ID.
     * The feedback's ID field is ignored as the database generates it.
     * 
     * @param feedback PatientFeedback object with required fields populated
     * @return The auto-generated database ID, or -1 if insert failed
     * @throws SQLException If insert fails
     */
    @Override
    public int insert(PatientFeedback feedback) throws SQLException {
        String sql = "INSERT INTO PatientFeedback (PatientID, DoctorID, Rating, Comments, FeedbackDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindPatientFeedback(stmt, feedback);
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
     * Updates an existing patient feedback with new values.
     * The feedback's ID must be set and match an existing record.
     * 
     * @param feedback PatientFeedback object with ID and updated fields
     * @throws SQLException If update fails or ID doesn't exist
     */
    @Override
    public void update(PatientFeedback feedback) throws SQLException {
        String sql = "UPDATE PatientFeedback SET PatientID = ?, DoctorID = ?, Rating = ?, Comments = ?, FeedbackDate = ? WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPatientFeedback(stmt, feedback);
            stmt.setInt(6, feedback.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Permanently deletes a patient feedback from the database.
     * 
     * @param id The ID of the feedback to delete
     * @throws SQLException If delete fails
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM PatientFeedback WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Binds patient feedback field values to a prepared statement.
     * 
     * <p>Helper method used by insert and update operations to set all fields
     * in the correct order. Handles null values properly.
     * 
     * @param stmt The prepared statement to bind parameters to
     * @param feedback The feedback whose data should be bound
     * @throws SQLException If parameter binding fails
     */
    private void bindPatientFeedback(PreparedStatement stmt, PatientFeedback feedback) throws SQLException {
        stmt.setInt(1, feedback.getPatientId());
        stmt.setInt(2, feedback.getDoctorId());
        if (feedback.getRating() != null) {
            stmt.setInt(3, feedback.getRating());
        } else {
            stmt.setNull(3, java.sql.Types.INTEGER);
        }
        stmt.setString(4, feedback.getComments());
        if (feedback.getFeedbackDate() != null) {
            stmt.setTimestamp(5, Timestamp.valueOf(feedback.getFeedbackDate()));
        } else {
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    /**
     * Maps a database result set row to a PatientFeedback object.
     * 
     * <p>Helper method that extracts all fields from the current row
     * of a result set and constructs a PatientFeedback instance. Handles null values
     * and type conversions.
     * 
     * @param rs The result set positioned at the row to map
     * @return A PatientFeedback object populated with data from the result set
     * @throws SQLException If column access fails or columns are missing
     */
    private PatientFeedback mapPatientFeedback(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        int patientId = rs.getInt("PatientID");
        int doctorId = rs.getInt("DoctorID");
        Integer rating = rs.getObject("Rating") != null ? rs.getInt("Rating") : null;
        String comments = rs.getString("Comments");
        Timestamp feedbackTimestamp = rs.getTimestamp("FeedbackDate");
        LocalDateTime feedbackDate = feedbackTimestamp != null ? feedbackTimestamp.toLocalDateTime() : null;
        String patientName = rs.getString("PatientName");
        String doctorName = rs.getString("DoctorName");
        return new PatientFeedback(id, patientId, doctorId, rating, comments, feedbackDate, patientName, doctorName);
    }
}
