package com.nks.hms.repository;

import com.nks.hms.db.Database;
import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.Statement;

public class PatientRepository {
    private static final String BASE_SELECT = "SELECT ID, FirstName, MiddleName, LastName, Email, PhoneNumber, DateOfBirth, Address FROM patient";

    public List<Patient> find(String searchTerm, int limit, int offset) throws SQLException {
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

    public int count(String searchTerm) throws SQLException {
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

    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE patient SET FirstName = ?, MiddleName = ?, LastName = ?, Email = ?, PhoneNumber = ?, DateOfBirth = ?, Address = ? WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPatient(stmt, patient);
            stmt.setInt(8, patient.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM patient WHERE ID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<VisitHistory> fetchHistory(int patientId) throws SQLException {
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
