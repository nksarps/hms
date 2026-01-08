package com.nks.hms.validation;

import com.nks.hms.model.Patient;
import java.util.Optional;

/**
 * Validator for Patient entities.
 * Implements single responsibility principle by separating validation logic.
 */
public class PatientValidator implements IValidator<Patient> {
    
    @Override
    public Optional<String> validate(Patient patient) {
        if (patient == null) {
            return Optional.of("Patient cannot be null");
        }
        
        if (patient.getFirstName() == null || patient.getFirstName().isBlank()) {
            return Optional.of("First name is required");
        }
        
        if (patient.getLastName() == null || patient.getLastName().isBlank()) {
            return Optional.of("Last name is required");
        }
        
        if (patient.getDateOfBirth() == null) {
            return Optional.of("Date of birth is required");
        }
        
        if (patient.getPhone() == null || patient.getPhone().isBlank()) {
            return Optional.of("Phone is required");
        }
        
        if (patient.getPhone().length() < 7) {
            return Optional.of("Phone must be at least 7 digits");
        }
        
        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            return Optional.of("Email is required");
        }
        
        if (!isValidEmail(patient.getEmail())) {
            return Optional.of("Email format is invalid");
        }
        
        return Optional.empty();
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
