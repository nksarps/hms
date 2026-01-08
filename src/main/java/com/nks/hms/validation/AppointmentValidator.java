package com.nks.hms.validation;

import com.nks.hms.model.Appointment;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Validator for Appointment entities.
 * Implements single responsibility principle by separating validation logic.
 */
public class AppointmentValidator implements IValidator<Appointment> {
    
    @Override
    public Optional<String> validate(Appointment appointment) {
        if (appointment == null) {
            return Optional.of("Appointment cannot be null");
        }
        
        if (appointment.getPatientId() == null || appointment.getPatientId() <= 0) {
            return Optional.of("Patient selection is required");
        }
        
        if (appointment.getDoctorId() == null || appointment.getDoctorId() <= 0) {
            return Optional.of("Doctor selection is required");
        }
        
        if (appointment.getAppointmentDate() == null) {
            return Optional.of("Appointment date and time are required");
        }
        
        // Allow today and future dates; block any date before today
        if (appointment.getAppointmentDate().toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
            return Optional.of("Appointment date must be today or in the future");
        }
        
        if (appointment.getReason() == null || appointment.getReason().isBlank()) {
            return Optional.of("Reason for appointment is required");
        }
        
        if (appointment.getReason().length() > 255) {
            return Optional.of("Reason must not exceed 255 characters");
        }
        
        return Optional.empty();
    }
}
