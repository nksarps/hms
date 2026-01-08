package com.nks.hms.validation;

import com.nks.hms.model.PatientFeedback;
import java.util.Optional;

/**
 * Validator for PatientFeedback entities.
 * Implements single responsibility principle by separating validation logic.
 */
public class PatientFeedbackValidator implements IValidator<PatientFeedback> {
    
    @Override
    public Optional<String> validate(PatientFeedback feedback) {
        if (feedback == null) {
            return Optional.of("Patient feedback cannot be null");
        }
        
        if (feedback.getPatientId() == null) {
            return Optional.of("Patient is required");
        }
        
        if (feedback.getDoctorId() == null) {
            return Optional.of("Doctor is required");
        }
        
        if (feedback.getRating() != null) {
            if (feedback.getRating() < 1 || feedback.getRating() > 5) {
                return Optional.of("Rating must be between 1 and 5");
            }
        }
        
        return Optional.empty();
    }
}
