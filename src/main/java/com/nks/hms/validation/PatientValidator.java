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
        if (!isValidName(patient.getFirstName())) {
            return Optional.of("First name must contain only letters");
        }
        
        if (patient.getLastName() == null || patient.getLastName().isBlank()) {
            return Optional.of("Last name is required");
        }
        if (!isValidName(patient.getLastName())) {
            return Optional.of("Last name must contain only letters");
        }
        
        if (patient.getDateOfBirth() == null) {
            return Optional.of("Date of birth is required");
        }
        
        if (patient.getPhone() == null || patient.getPhone().isBlank()) {
            return Optional.of("Phone is required");
        }
        if (!isValidPhone(patient.getPhone())) {
            return Optional.of("Phone must contain only digits and be 7-15 digits long");
        }
        
        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            return Optional.of("Email is required");
        }
        
        if (!isValidEmail(patient.getEmail())) {
            return Optional.of("Email format is invalid");
        }
        
        return Optional.empty();
    }
    
    private boolean isValidName(String name) {
        return name.matches("^[A-Za-z]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9]{7,15}$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z]+\\.[A-Za-z]+$");
    }
}
