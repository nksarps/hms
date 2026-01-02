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

public class DoctorRepository {
    private static final String BASE_SELECT = "SELECT ID, FirstName, MiddleName, LastName, Email, PhoneNumber FROM doctor";

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

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM doctor WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

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
