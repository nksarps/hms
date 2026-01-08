package com.nks.hms.model;

import java.time.LocalDateTime;

/**
 * Domain model representing patient feedback in the hospital management system.
 * Contains feedback information including ratings and comments about doctors.
 * Maps directly to the 'PatientFeedback' table in the MySQL database.
 * 
 * @see com.nks.hms.repository.PatientFeedbackRepository
 */
public class PatientFeedback {
    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private Integer rating;
    private String comments;
    private LocalDateTime feedbackDate;
    
    // Display fields (not in database)
    private String patientName;
    private String doctorName;

    /**
     * Default no-arg constructor required by JavaFX for data binding
     * and by JDBC when creating instances from result sets.
     */
    public PatientFeedback() {
    }

    /**
     * Full constructor for creating a patient feedback with all fields initialized.
     * Typically used when mapping database results to PatientFeedback objects.
     * 
     * @param id Database primary key, null for new feedback
     * @param patientId Patient's ID (required)
     * @param doctorId Doctor's ID (required)
     * @param rating Rating value (optional)
     * @param comments Feedback comments (optional)
     * @param feedbackDate Date and time of feedback (required)
     */
    public PatientFeedback(Integer id, Integer patientId, Integer doctorId, Integer rating, 
                          String comments, LocalDateTime feedbackDate) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
    }

    /**
     * Full constructor including display names.
     * 
     * @param id Database primary key
     * @param patientId Patient's ID
     * @param doctorId Doctor's ID
     * @param rating Rating value
     * @param comments Feedback comments
     * @param feedbackDate Date and time of feedback
     * @param patientName Patient's display name
     * @param doctorName Doctor's display name
     */
    public PatientFeedback(Integer id, Integer patientId, Integer doctorId, Integer rating, 
                          String comments, LocalDateTime feedbackDate, 
                          String patientName, String doctorName) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
        this.patientName = patientName;
        this.doctorName = doctorName;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(LocalDateTime feedbackDate) {
        this.feedbackDate = feedbackDate;
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
        return "PatientFeedback{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", rating=" + rating +
                ", comments='" + comments + '\'' +
                ", feedbackDate=" + feedbackDate +
                ", patientName='" + patientName + '\'' +
                ", doctorName='" + doctorName + '\'' +
                '}';
    }
}
