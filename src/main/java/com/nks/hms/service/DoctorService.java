package com.nks.hms.service;

import com.nks.hms.model.Doctor;
import com.nks.hms.repository.IDoctorRepository;
import com.nks.hms.validation.IValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Doctor service implementation providing business logic.
 * Wraps DoctorCache and adds validation layer.
 * Follows single responsibility and dependency inversion principles.
 */
public class DoctorService implements IDoctorService {
    private final DoctorCache cache;
    private final IValidator<Doctor> validator;
    
    /**
     * Creates doctor service with dependency injection.
     * 
     * @param repository Doctor repository for data access
     * @param validator Doctor validator for business rules
     */
    public DoctorService(IDoctorRepository repository, IValidator<Doctor> validator) {
        this.cache = new DoctorCache(repository);
        this.validator = validator;
    }
    
    @Override
    public List<Doctor> searchDoctors(String searchTerm, int limit, int offset, String sortBy) throws SQLException {
        DoctorCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.find(searchTerm, limit, offset, sortOption);
    }
    
    @Override
    public int countDoctors(String searchTerm) throws SQLException {
        return cache.count(searchTerm);
    }
    
    @Override
    public Optional<Doctor> getDoctorById(int id) throws SQLException {
        return cache.findById(id);
    }
    
    @Override
    public int saveDoctor(Doctor doctor) throws SQLException {
        // Validate before saving
        Optional<String> error = validator.validate(doctor);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        doctor.setFirstName(doctor.getFirstName().trim());
        if (doctor.getMiddleName() != null) {
            doctor.setMiddleName(doctor.getMiddleName().trim());
        }
        doctor.setLastName(doctor.getLastName().trim());
        doctor.setPhone(doctor.getPhone().trim());
        doctor.setEmail(doctor.getEmail().trim());
        
        // Insert or update
        if (doctor.getId() == null) {
            return cache.insert(doctor);
        } else {
            cache.update(doctor);
            return doctor.getId();
        }
    }
    
    @Override
    public void deleteDoctor(int id) throws SQLException {
        cache.delete(id);
    }
    
    @Override
    public String getCacheStats() {
        return cache.getCacheStats();
    }
    
    /**
     * Converts UI sort string to cache sort enum.
     */
    private DoctorCache.SortBy parseSortBy(String uiValue) {
        if (uiValue == null) return DoctorCache.SortBy.ID_DESC;
        return switch (uiValue) {
            case "ID (Oldest)" -> DoctorCache.SortBy.ID_ASC;
            case "Name (A-Z)" -> DoctorCache.SortBy.NAME_ASC;
            case "Name (Z-A)" -> DoctorCache.SortBy.NAME_DESC;
            default -> DoctorCache.SortBy.ID_DESC;
        };
    }
}
