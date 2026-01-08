package com.nks.hms.validation;

import com.nks.hms.model.Doctor;
import java.util.Optional;

/**
 * Validator for Doctor entities.
 * Implements single responsibility principle by separating validation logic.
 */
public class DoctorValidator implements IValidator<Doctor> {
    
    @Override
    public Optional<String> validate(Doctor doctor) {
        if (doctor == null) {
            return Optional.of("Doctor cannot be null");
        }
        
        if (doctor.getFirstName() == null || doctor.getFirstName().isBlank()) {
            return Optional.of("First name is required");
        }
        
        if (doctor.getLastName() == null || doctor.getLastName().isBlank()) {
            return Optional.of("Last name is required");
        }
        
        if (doctor.getPhone() == null || doctor.getPhone().isBlank()) {
            return Optional.of("Phone is required");
        }
        
        if (doctor.getPhone().length() < 7) {
            return Optional.of("Phone must be at least 7 digits");
        }
        
        if (doctor.getEmail() == null || doctor.getEmail().isBlank()) {
            return Optional.of("Email is required");
        }
        
        if (!isValidEmail(doctor.getEmail())) {
            return Optional.of("Email format is invalid");
        }
        
        return Optional.empty();
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
