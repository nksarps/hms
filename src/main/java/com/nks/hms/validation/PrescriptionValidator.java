package com.nks.hms.validation;

import com.nks.hms.model.Prescription;
import java.util.Optional;

/**
 * Validator for Prescription entities.
 * Implements single responsibility principle by separating validation logic.
 */
public class PrescriptionValidator implements IValidator<Prescription> {
    
    @Override
    public Optional<String> validate(Prescription prescription) {
        if (prescription == null) {
            return Optional.of("Prescription cannot be null");
        }
        
        if (prescription.getPatientId() == null) {
            return Optional.of("Patient is required");
        }
        
        if (prescription.getDoctorId() == null) {
            return Optional.of("Doctor is required");
        }
        
        if (prescription.getPrescriptionDate() == null) {
            return Optional.of("Prescription date is required");
        }
        
        return Optional.empty();
    }
}
