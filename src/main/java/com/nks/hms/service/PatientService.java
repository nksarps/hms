package com.nks.hms.service;

import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
import com.nks.hms.repository.IPatientRepository;
import com.nks.hms.validation.IValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Patient service implementation providing business logic.
 * Wraps PatientCache and adds validation layer.
 * Follows single responsibility and dependency inversion principles.
 */
public class PatientService implements IPatientService {
    private final PatientCache cache;
    private final IValidator<Patient> validator;
    
    /**
     * Creates patient service with dependency injection.
     * 
     * @param repository Patient repository for data access
     * @param validator Patient validator for business rules
     */
    public PatientService(IPatientRepository repository, IValidator<Patient> validator) {
        this.cache = new PatientCache(repository);
        this.validator = validator;
    }
    
    @Override
    public List<Patient> searchPatients(String searchTerm, int limit, int offset, String sortBy) throws SQLException {
        PatientCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.find(searchTerm, limit, offset, sortOption);
    }
    
    @Override
    public int countPatients(String searchTerm) throws SQLException {
        return cache.count(searchTerm);
    }
    
    @Override
    public Optional<Patient> getPatientById(int id) throws SQLException {
        return cache.findById(id);
    }
    
    @Override
    public List<VisitHistory> getVisitHistory(int patientId) throws SQLException {
        return cache.getHistory(patientId);
    }
    
    @Override
    public int savePatient(Patient patient) throws SQLException {
        // Validate before saving
        Optional<String> error = validator.validate(patient);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        patient.setFirstName(patient.getFirstName().trim());
        if (patient.getMiddleName() != null) {
            patient.setMiddleName(patient.getMiddleName().trim());
        }
        patient.setLastName(patient.getLastName().trim());
        patient.setPhone(patient.getPhone().trim());
        patient.setEmail(patient.getEmail().trim());
        
        // Insert or update
        if (patient.getId() == null) {
            return cache.insert(patient);
        } else {
            cache.update(patient);
            return patient.getId();
        }
    }
    
    @Override
    public void deletePatient(int id) throws SQLException {
        cache.delete(id);
    }
    
    @Override
    public String getCacheStats() {
        return cache.getCacheStats();
    }
    
    /**
     * Converts UI sort string to cache sort enum.
     */
    private PatientCache.SortBy parseSortBy(String uiValue) {
        if (uiValue == null) return PatientCache.SortBy.ID_DESC;
        return switch (uiValue) {
            case "ID (Oldest)" -> PatientCache.SortBy.ID_ASC;
            case "Name (A-Z)" -> PatientCache.SortBy.NAME_ASC;
            case "Name (Z-A)" -> PatientCache.SortBy.NAME_DESC;
            case "DOB (Oldest)" -> PatientCache.SortBy.DOB_ASC;
            case "DOB (Newest)" -> PatientCache.SortBy.DOB_DESC;
            default -> PatientCache.SortBy.ID_DESC;
        };
    }
}
