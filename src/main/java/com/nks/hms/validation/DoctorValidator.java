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
        if (!isValidName(doctor.getFirstName())) {
            return Optional.of("First name must contain only letters");
        }
        
        if (doctor.getLastName() == null || doctor.getLastName().isBlank()) {
            return Optional.of("Last name is required");
        }
        if (!isValidName(doctor.getLastName())) {
            return Optional.of("Last name must contain only letters");
        }
        
        if (doctor.getPhone() == null || doctor.getPhone().isBlank()) {
            return Optional.of("Phone is required");
        }
        if (!isValidPhone(doctor.getPhone())) {
            return Optional.of("Phone must contain only digits and be 7-15 digits long");
        }
        
        if (doctor.getEmail() == null || doctor.getEmail().isBlank()) {
            return Optional.of("Email is required");
        }
        
        if (!isValidEmail(doctor.getEmail())) {
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
