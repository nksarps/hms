package com.nks.hms.service;

import com.nks.hms.model.Prescription;
import com.nks.hms.repository.IPrescriptionRepository;
import com.nks.hms.validation.IValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Prescription service implementation providing business logic.
 * Wraps PrescriptionCache and adds validation layer.
 * Follows single responsibility and dependency inversion principles.
 */
public class PrescriptionService implements IPrescriptionService {
    private final PrescriptionCache cache;
    private final IValidator<Prescription> validator;
    
    /**
     * Creates prescription service with dependency injection.
     * 
     * @param repository Prescription repository for data access
     * @param validator Prescription validator for business rules
     */
    public PrescriptionService(IPrescriptionRepository repository, IValidator<Prescription> validator) {
        this.cache = new PrescriptionCache(repository);
        this.validator = validator;
    }
    
    @Override
    public List<Prescription> searchPrescriptions(String searchTerm, int limit, int offset, String sortBy) throws SQLException {
        PrescriptionCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.find(searchTerm, limit, offset, sortOption);
    }
    
    @Override
    public int countPrescriptions(String searchTerm) throws SQLException {
        return cache.count(searchTerm);
    }
    
    @Override
    public Optional<Prescription> getPrescriptionById(int id) throws SQLException {
        return cache.findById(id);
    }
    
    @Override
    public int savePrescription(Prescription prescription) throws SQLException {
        // Validate before saving
        Optional<String> error = validator.validate(prescription);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        if (prescription.getNotes() != null && !prescription.getNotes().isBlank()) {
            prescription.setNotes(prescription.getNotes().trim());
        }
        
        // Insert or update
        if (prescription.getId() == null) {
            return cache.insert(prescription);
        } else {
            cache.update(prescription);
            return prescription.getId();
        }
    }
    
    @Override
    public void deletePrescription(int id) throws SQLException {
        cache.delete(id);
    }
    
    @Override
    public String getCacheStats() {
        return cache.getCacheStats();
    }
    
    /**
     * Converts UI sort string to cache sort enum.
     */
    private PrescriptionCache.SortBy parseSortBy(String uiValue) {
        if (uiValue == null) return PrescriptionCache.SortBy.NONE;
        return switch (uiValue) {
            case "All" -> PrescriptionCache.SortBy.NONE;
            case "Date (Oldest)" -> PrescriptionCache.SortBy.DATE_ASC;
            case "Date (Newest)" -> PrescriptionCache.SortBy.DATE_DESC;
            case "Patient (A-Z)" -> PrescriptionCache.SortBy.PATIENT_ASC;
            case "Patient (Z-A)" -> PrescriptionCache.SortBy.PATIENT_DESC;
            case "Doctor (A-Z)" -> PrescriptionCache.SortBy.DOCTOR_ASC;
            case "Doctor (Z-A)" -> PrescriptionCache.SortBy.DOCTOR_DESC;
            default -> PrescriptionCache.SortBy.NONE;
        };
    }
}
