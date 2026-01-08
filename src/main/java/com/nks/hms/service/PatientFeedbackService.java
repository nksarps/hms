package com.nks.hms.service;

import com.nks.hms.model.PatientFeedback;
import com.nks.hms.repository.IPatientFeedbackRepository;
import com.nks.hms.validation.IValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Patient feedback service implementation providing business logic.
 * Wraps PatientFeedbackCache and adds validation layer.
 * Follows single responsibility and dependency inversion principles.
 */
public class PatientFeedbackService implements IPatientFeedbackService {
    private final PatientFeedbackCache cache;
    private final IValidator<PatientFeedback> validator;
    
    /**
     * Creates patient feedback service with dependency injection.
     * 
     * @param repository Patient feedback repository for data access
     * @param validator Patient feedback validator for business rules
     */
    public PatientFeedbackService(IPatientFeedbackRepository repository, IValidator<PatientFeedback> validator) {
        this.cache = new PatientFeedbackCache(repository);
        this.validator = validator;
    }
    
    @Override
    public List<PatientFeedback> searchFeedback(String searchTerm, int limit, int offset, String sortBy) throws SQLException {
        PatientFeedbackCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.find(searchTerm, limit, offset, sortOption);
    }
    
    @Override
    public int countFeedback(String searchTerm) throws SQLException {
        return cache.count(searchTerm);
    }
    
    @Override
    public Optional<PatientFeedback> getFeedbackById(int id) throws SQLException {
        return cache.findById(id);
    }
    
    @Override
    public int saveFeedback(PatientFeedback feedback) throws SQLException {
        // Validate before saving
        Optional<String> error = validator.validate(feedback);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        if (feedback.getComments() != null && !feedback.getComments().isBlank()) {
            feedback.setComments(feedback.getComments().trim());
        }
        
        // Insert or update
        if (feedback.getId() == null) {
            return cache.insert(feedback);
        } else {
            cache.update(feedback);
            return feedback.getId();
        }
    }
    
    @Override
    public void deleteFeedback(int id) throws SQLException {
        cache.delete(id);
    }
    
    @Override
    public String getCacheStats() {
        return cache.getCacheStats();
    }
    
    /**
     * Converts UI sort string to cache sort enum.
     */
    private PatientFeedbackCache.SortBy parseSortBy(String uiValue) {
        if (uiValue == null) return PatientFeedbackCache.SortBy.NONE;
        return switch (uiValue) {
            case "All" -> PatientFeedbackCache.SortBy.NONE;
            case "Date (Oldest)" -> PatientFeedbackCache.SortBy.DATE_ASC;
            case "Date (Newest)" -> PatientFeedbackCache.SortBy.DATE_DESC;
            case "Patient (A-Z)" -> PatientFeedbackCache.SortBy.PATIENT_ASC;
            case "Patient (Z-A)" -> PatientFeedbackCache.SortBy.PATIENT_DESC;
            case "Doctor (A-Z)" -> PatientFeedbackCache.SortBy.DOCTOR_ASC;
            case "Doctor (Z-A)" -> PatientFeedbackCache.SortBy.DOCTOR_DESC;
            case "Rating (Low-High)" -> PatientFeedbackCache.SortBy.RATING_ASC;
            case "Rating (High-Low)" -> PatientFeedbackCache.SortBy.RATING_DESC;
            default -> PatientFeedbackCache.SortBy.NONE;
        };
    }
}
