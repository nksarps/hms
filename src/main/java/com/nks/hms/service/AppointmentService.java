package com.nks.hms.service;

import com.nks.hms.model.Appointment;
import com.nks.hms.repository.IAppointmentRepository;
import com.nks.hms.validation.IValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Appointment service implementation providing business logic.
 * Wraps AppointmentCache and adds validation layer.
 * Follows single responsibility and dependency inversion principles.
 */
public class AppointmentService implements IAppointmentService {
    private final AppointmentCache cache;
    private final IValidator<Appointment> validator;
    
    /**
     * Creates appointment service with dependency injection.
     * 
     * @param repository Appointment repository for data access
     * @param validator Appointment validator for business rules
     */
    public AppointmentService(IAppointmentRepository repository, IValidator<Appointment> validator) {
        this.cache = new AppointmentCache(repository);
        this.validator = validator;
    }
    
    @Override
    public List<Appointment> searchAppointments(String searchTerm, int limit, int offset, String sortBy) throws SQLException {
        AppointmentCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.find(searchTerm, limit, offset, sortOption);
    }
    
    @Override
    public int countAppointments(String searchTerm, String sortBy) throws SQLException {
        AppointmentCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.count(searchTerm, sortOption);
    }
    
    @Override
    public Optional<Appointment> getAppointmentById(int id) throws SQLException {
        return cache.findById(id);
    }
    
    @Override
    public int saveAppointment(Appointment appointment) throws SQLException {
        // Validate before saving
        Optional<String> error = validator.validate(appointment);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        if (appointment.getReason() != null) {
            appointment.setReason(appointment.getReason().trim());
        }
        
        if (appointment.getId() == null) {
            // New appointment
            return cache.create(appointment);
        } else {
            // Update existing
            cache.update(appointment);
            return appointment.getId();
        }
    }
    
    @Override
    public void updateAppointment(Appointment appointment) throws SQLException {
        // Validate before updating
        Optional<String> error = validator.validate(appointment);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        if (appointment.getReason() != null) {
            appointment.setReason(appointment.getReason().trim());
        }
        
        cache.update(appointment);
    }
    
    @Override
    public void deleteAppointment(int id) throws SQLException {
        cache.delete(id);
    }
    
    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId) throws SQLException {
        return cache.findByPatientId(patientId);
    }
    
    @Override
    public List<Appointment> getAppointmentsByDoctor(int doctorId) throws SQLException {
        return cache.findByDoctorId(doctorId);
    }
    
    @Override
    public String getCacheStats() {
        return cache.getStats();
    }
    
    /**
     * Parses sort option string to enum value.
     */
    private AppointmentCache.SortBy parseSortBy(String sortBy) {
        if (sortBy == null) {
            return AppointmentCache.SortBy.DATE_DESC;
        }
        
        return switch (sortBy.toUpperCase()) {
            case "TODAY" -> AppointmentCache.SortBy.TODAY;
            case "NEXT_7" -> AppointmentCache.SortBy.NEXT_7;
            case "NEXT_30" -> AppointmentCache.SortBy.NEXT_30;
            case "DATE_ASC" -> AppointmentCache.SortBy.DATE_ASC;
            default -> AppointmentCache.SortBy.DATE_DESC;
        };
    }
}
