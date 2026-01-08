package com.nks.hms.model;

import java.time.LocalDateTime;

/**
 * Domain model representing an appointment in the hospital management system.
 * Contains information about scheduled appointments between patients and doctors.
 * Maps directly to the 'appointment' table in the MySQL database.
 * 
 * @see com.nks.hms.repository.AppointmentRepository
 */
public class Appointment {
    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private LocalDateTime appointmentDate;
    private String reason;
    
    // Additional fields for display (not persisted)
    private String patientName;
    private String doctorName;

    /**
     * Default no-arg constructor required by JavaFX for data binding
     * and by JDBC when creating instances from result sets.
     */
    public Appointment() {
    }

    /**
     * Constructor for creating an appointment with core fields initialized.
     * 
     * @param id Database primary key, null for new appointments
     * @param patientId Foreign key to patient (required)
     * @param doctorId Foreign key to doctor (required)
     * @param appointmentDate Scheduled date and time (required)
     * @param reason Reason for appointment (optional)
     */
    public Appointment(Integer id, Integer patientId, Integer doctorId, LocalDateTime appointmentDate, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
    }

    /**
     * Full constructor including display fields.
     * 
     * @param id Database primary key
     * @param patientId Foreign key to patient
     * @param doctorId Foreign key to doctor
     * @param appointmentDate Scheduled date and time
     * @param reason Reason for appointment
     * @param patientName Patient's name for display
     * @param doctorName Doctor's name for display
     */
    public Appointment(Integer id, Integer patientId, Integer doctorId, LocalDateTime appointmentDate, 
                      String reason, String patientName, String doctorName) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
        this.patientName = patientName;
        this.doctorName = doctorName;
    }

    // Getters and Setters
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentDate=" + appointmentDate +
                ", reason='" + reason + '\'' +
                ", patientName='" + patientName + '\'' +
                ", doctorName='" + doctorName + '\'' +
                '}';
    }
}
