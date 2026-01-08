package com.nks.hms.model;

import java.time.LocalDate;

/**
 * Domain model representing a prescription in the hospital management system.
 * Contains patient, doctor, prescription date, and notes.
 * Maps directly to the 'Prescription' table in the MySQL database.
 * 
 * @see com.nks.hms.repository.PrescriptionRepository
 */
public class Prescription {
    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private String patientName;
    private String doctorName;
    private LocalDate prescriptionDate;
    private String notes;

    /**
     * Default no-arg constructor required by JavaFX for data binding
     * and by JDBC when creating instances from result sets.
     */
    public Prescription() {
    }

    /**
     * Full constructor for creating a prescription with all fields initialized.
     * Typically used when mapping database results to Prescription objects.
     * 
     * @param id Database primary key, null for new prescriptions
     * @param patientId Patient's ID (required)
     * @param doctorId Doctor's ID (required)
     * @param patientName Patient's full name (for display purposes)
     * @param doctorName Doctor's full name (for display purposes)
     * @param prescriptionDate Date prescription was issued (required)
     * @param notes Additional notes (optional)
     */
    public Prescription(Integer id, Integer patientId, Integer doctorId, String patientName, 
                       String doctorName, LocalDate prescriptionDate, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.prescriptionDate = prescriptionDate;
        this.notes = notes;
    }

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

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", patientName='" + patientName + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", prescriptionDate=" + prescriptionDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
