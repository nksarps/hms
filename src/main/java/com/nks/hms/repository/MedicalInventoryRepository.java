package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.MedicalInventory;
import java.math.BigDecimal;
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
 * Repository layer for medical inventory data access using JDBC.
 * 
 * <p>Provides CRUD operations and search functionality for medical inventory records.
 * All methods execute direct SQL queries against the 'MedicalInventory' table in the MySQL database.
 * Search operations support case-insensitive filtering across multiple fields
 * (name, type).
 * 
 * <p>Key features:
 * <ul>
 *   <li>Paginated search with configurable limit and offset</li>
 *   <li>Case-insensitive LIKE queries for flexible searching</li>
 *   <li>Parameterized queries to prevent SQL injection</li>
 * </ul>
 * 
 * <p>All database operations use try-with-resources to ensure proper connection cleanup.
 * Methods throw SQLException which should be handled by the caller (typically the UI layer).
 * 
 * @see com.nks.hms.model.MedicalInventory
 * @see com.nks.hms.db.Database
 */
public class MedicalInventoryRepository implements IMedicalInventoryRepository {
    private static final String BASE_SELECT = "SELECT ID, Name, Type, Quantity, Unit, ExpiryDate, Cost FROM MedicalInventory";

    /**
     * Searches for medical inventory items matching the given search term with pagination support.
     * 
     * <p>The search is case-insensitive and matches against name and type.
     * Results are ordered by ID descending (newest first).
     * 
     * @param searchTerm Text to search for (can be null or empty to return all items)
     * @param limit Maximum number of records to return (page size)
     * @param offset Number of records to skip (for pagination)
     * @return List of matching medical inventory items (empty list if no matches)
     * @throws SQLException If database query fails
     */
    @Override
    public List<MedicalInventory> find(String searchTerm, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<String> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE Name LIKE ? OR Type LIKE ?");
            String pattern = "%" + searchTerm.trim() + "%";
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

            List<MedicalInventory> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapMedicalInventory(rs));
                }
            }
            return results;
        }
    }

    /**
     * Counts the total number of medical inventory items matching the given search term.
     * Uses the same filtering logic as find() to ensure consistency.
     * 
     * @param searchTerm Text to search for (can be null or empty to count all)
     * @return Total number of matching medical inventory records
     * @throws SQLException If database query fails
     */
    @Override
    public int count(String searchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM MedicalInventory");
        List<String> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append(" WHERE Name LIKE ? OR Type LIKE ?");
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
     * Retrieves a single medical inventory item by its unique database ID.
     * 
     * @param id The medical inventory item's primary key
     * @return Optional containing the item if found, empty otherwise
     * @throws SQLException If database query fails
     */
    @Override
    public Optional<MedicalInventory> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMedicalInventory(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new medical inventory item and returns the auto-generated ID.
     * The item's ID field is ignored as the database generates it.
     * 
     * @param item MedicalInventory object with required fields populated
     * @return The auto-generated database ID, or -1 if insert failed
     * @throws SQLException If insert fails
     */
    @Override
    public int insert(MedicalInventory item) throws SQLException {
        String sql = "INSERT INTO MedicalInventory (Name, Type, Quantity, Unit, ExpiryDate, Cost) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindMedicalInventory(stmt, item);
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
     * Updates an existing medical inventory item with new values.
     * The item's ID must be set and match an existing record.
     * 
     * @param item MedicalInventory object with ID and updated fields
     * @throws SQLException If update fails or ID doesn't exist
     */
    @Override
    public void update(MedicalInventory item) throws SQLException {
        String sql = "UPDATE MedicalInventory SET Name = ?, Type = ?, Quantity = ?, Unit = ?, ExpiryDate = ?, Cost = ? WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindMedicalInventory(stmt, item);
            stmt.setInt(7, item.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Permanently deletes a medical inventory item from the database.
     * 
     * @param id The ID of the medical inventory item to delete
     * @throws SQLException If delete fails (e.g., foreign key constraint)
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM MedicalInventory WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Binds medical inventory field values to a prepared statement.
     * 
     * <p>Helper method used by insert and update operations to set all fields
     * in the correct order. Handles null values properly.
     * 
     * @param stmt The prepared statement to bind parameters to
     * @param item The medical inventory item whose data should be bound
     * @throws SQLException If parameter binding fails
     */
    private void bindMedicalInventory(PreparedStatement stmt, MedicalInventory item) throws SQLException {
        stmt.setString(1, item.getName());
        stmt.setString(2, item.getType());
        if (item.getQuantity() != null) {
            stmt.setInt(3, item.getQuantity());
        } else {
            stmt.setNull(3, java.sql.Types.INTEGER);
        }
        stmt.setString(4, item.getUnit());
        if (item.getExpiryDate() != null) {
            stmt.setDate(5, Date.valueOf(item.getExpiryDate()));
        } else {
            stmt.setNull(5, java.sql.Types.DATE);
        }
        if (item.getCost() != null) {
            stmt.setBigDecimal(6, item.getCost());
        } else {
            stmt.setNull(6, java.sql.Types.DECIMAL);
        }
    }

    /**
     * Maps a database result set row to a MedicalInventory object.
     * 
     * <p>Helper method that extracts all fields from the current row
     * of a result set and constructs a MedicalInventory instance. Handles null values
     * and type conversions.
     * 
     * @param rs The result set positioned at the row to map
     * @return A MedicalInventory object populated with data from the result set
     * @throws SQLException If column access fails or columns are missing
     */
    private MedicalInventory mapMedicalInventory(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        String name = rs.getString("Name");
        String type = rs.getString("Type");
        Integer quantity = rs.getObject("Quantity") != null ? rs.getInt("Quantity") : null;
        String unit = rs.getString("Unit");
        Date expiryDate = rs.getDate("ExpiryDate");
        LocalDate expiry = expiryDate != null ? expiryDate.toLocalDate() : null;
        BigDecimal cost = rs.getBigDecimal("Cost");
        return new MedicalInventory(id, name, type, quantity, unit, expiry, cost);
    }
}
