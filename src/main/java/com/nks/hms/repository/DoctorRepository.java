package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.Doctor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository layer for doctor data access using JDBC.
 * 
 * <p>Provides CRUD operations and search functionality for doctor records.
 * Similar to PatientRepository but simpler as doctors don't have history queries.
 * All methods execute direct SQL queries against the 'doctor' table.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Paginated search with limit and offset</li>
 *   <li>Case-insensitive LIKE queries across name, phone, and email</li>
 *   <li>Optional department association via nullable departmentId field</li>
 *   <li>Parameterized queries for SQL injection prevention</li>
 * </ul>
 * 
 * @see com.nks.hms.model.Doctor
 * @see com.nks.hms.db.Database
 */
// JDBC CRUD for doctors with simple text search and pagination.
public class DoctorRepository {
    private static final String BASE_SELECT = "SELECT ID, FirstName, MiddleName, LastName, Email, PhoneNumber FROM doctor";

    /**
     * Searches for doctors matching the search term with pagination.
     * Case-insensitive search across first name, last name, phone, and email.
     * 
     * @param searchTerm Text to search for (null or empty returns all)
     * @param limit Maximum records to return (page size)
     * @param offset Records to skip (for pagination)
     * @return List of matching doctors (empty if none)
     * @throws SQLException If database query fails
     */
    // Case-insensitive search across name/phone/email; ordered by newest first.
    public List<Doctor> find(String searchTerm, int limit, int offset) throws SQLException {
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

            List<Doctor> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapDoctor(rs));
                }
            }
            return results;
        }
    }

    /**
     * Counts total doctors matching the search term.
     * Uses same filtering as find() for consistency.
     * 
     * @param searchTerm Text to search for
     * @return Total matching doctor records
     * @throws SQLException If query fails
     */
    // Count rows matching the search filters used in find().
    public int count(String searchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM doctor");
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
     * Retrieves a single doctor by database ID.
     * 
     * @param id Doctor's primary key
     * @return Optional with doctor if found, empty otherwise
     * @throws SQLException If query fails
     */
    // Fetch a single doctor by ID.
    public Optional<Doctor> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDoctor(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new doctor record and returns the generated ID.
     * DepartmentID can be null if doctor not yet assigned to a department.
     * 
     * @param doctor Doctor object with required fields
     * @return Auto-generated database ID, or -1 if failed
     * @throws SQLException If insert fails
     */
    // Insert a new doctor and return generated key.
    public int insert(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctor (FirstName, MiddleName, LastName, Email, PhoneNumber, DepartmentID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getMiddleName());
            stmt.setString(3, doctor.getLastName());
            stmt.setString(4, doctor.getEmail());
            stmt.setString(5, doctor.getPhone());
            if (doctor.getDepartmentId() != null) {
                stmt.setInt(6, doctor.getDepartmentId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
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
     * Updates an existing doctor record.
     * Doctor ID must be set and exist in database.
     * 
     * @param doctor Doctor object with ID and updated fields
     * @throws SQLException If update fails
     */
    // Update an existing doctor record.
    public void update(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctor SET FirstName = ?, MiddleName = ?, LastName = ?, Email = ?, PhoneNumber = ?, DepartmentID = ? WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getMiddleName());
            stmt.setString(3, doctor.getLastName());
            stmt.setString(4, doctor.getEmail());
            stmt.setString(5, doctor.getPhone());
            if (doctor.getDepartmentId() != null) {
                stmt.setInt(6, doctor.getDepartmentId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setInt(7, doctor.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Permanently deletes a doctor record.
     * Warning: Hard delete with no soft-delete mechanism.
     * 
     * @param id Doctor ID to delete
     * @throws SQLException If delete fails (e.g., foreign key constraint)
     */
    // Delete a doctor by ID.
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM doctor WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a result set row to a Doctor object.
     * Extracts doctor fields from current row and constructs instance.
     * 
     * @param rs Result set positioned at row to map
     * @return Doctor object with data from result set
     * @throws SQLException If column access fails
     */
    private Doctor mapDoctor(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        String first = rs.getString("FirstName");
        String middle = rs.getString("MiddleName");
        String last = rs.getString("LastName");
        String email = rs.getString("Email");
        String phone = rs.getString("PhoneNumber");
        return new Doctor(id, first, middle, last, phone, email);
    }
}