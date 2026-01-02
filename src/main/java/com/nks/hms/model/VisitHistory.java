package com.nks.hms.model;

import java.time.LocalDate;

/**
 * Immutable value object representing a patient's visit history record.
 * This is a read-only DTO (Data Transfer Object) used for displaying
 * appointment history in the UI. It combines data from the appointment
 * and doctor tables into a single view.
 * 
 * All fields are final and set via constructor to ensure immutability.
 * This class does not map to a specific database table but is constructed
 * from JOIN queries in the repository layer.
 * 
 * @see com.nks.hms.repository.PatientRepository#fetchHistory(int)
 */
public class VisitHistory {
    private final String doctorName;
    private final LocalDate visitDate;
    private final String reason;
    private final String notes;

    /**
     * Constructs a complete visit history record.
     * All fields are required except notes (can be empty string).
     * 
     * @param doctorName Full name of the doctor who conducted the visit
     * @param visitDate Date the appointment took place
     * @param reason Reason for the visit (chief complaint)
     * @param notes Additional notes about the visit (optional, can be empty)
     */
    public VisitHistory(String doctorName, LocalDate visitDate, String reason, String notes) {
        this.doctorName = doctorName;
        this.visitDate = visitDate;
        this.reason = reason;
        this.notes = notes;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public String getReason() {
        return reason;
    }

    public String getNotes() {
        return notes;
    }
}
