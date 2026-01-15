package com.nks.hms.model;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model representing unstructured patient notes stored in MongoDB.
 * 
 * <p>This class demonstrates the flexibility of NoSQL for storing varying data structures
 * compared to rigid SQL schemas. Each note can have different fields and nested data
 * without requiring schema migrations.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Flexible structure - notes can contain different types of information</li>
 *   <li>Nested data support - vital signs, diagnoses stored as complex objects</li>
 *   <li>No schema migration needed when adding new note types</li>
 *   <li>Full-text search capabilities on content field</li>
 * </ul>
 */
public class PatientNote {
    private ObjectId id;              // MongoDB's unique identifier
    private int patientId;            // Reference to MySQL Patient.ID
    private int doctorId;             // Reference to MySQL Doctor.ID
    private LocalDateTime timestamp;  // When the note was created
    private String noteType;          // "clinical", "discharge", "progress", "consultation", "emergency"
    private String content;           // Main unstructured text content
    private List<String> diagnoses;   // List of diagnosis codes or descriptions
    private List<Map<String, Object>> vitalSigns;  // Flexible structure for vital signs
    private Map<String, Object> metadata;          // Additional flexible data
    
    /**
     * Default constructor required for MongoDB serialization.
     */
    public PatientNote() {
        this.timestamp = LocalDateTime.now();
        this.diagnoses = new ArrayList<>();
        this.vitalSigns = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    /**
     * Constructor with essential fields.
     * 
     * @param patientId ID of the patient this note is about
     * @param doctorId ID of the doctor who created the note
     * @param noteType Type of note (clinical, discharge, etc.)
     * @param content Main text content of the note
     */
    public PatientNote(int patientId, int doctorId, String noteType, String content) {
        this();
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.noteType = noteType;
        this.content = content;
    }
    
    // Getters and Setters
    
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public int getPatientId() {
        return patientId;
    }
    
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    
    public int getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getNoteType() {
        return noteType;
    }
    
    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<String> getDiagnoses() {
        return diagnoses;
    }
    
    public void setDiagnoses(List<String> diagnoses) {
        this.diagnoses = diagnoses;
    }
    
    public List<Map<String, Object>> getVitalSigns() {
        return vitalSigns;
    }
    
    public void setVitalSigns(List<Map<String, Object>> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Convenience method to add a diagnosis to the list.
     * 
     * @param diagnosis Diagnosis code or description to add
     */
    public void addDiagnosis(String diagnosis) {
        if (this.diagnoses == null) {
            this.diagnoses = new ArrayList<>();
        }
        this.diagnoses.add(diagnosis);
    }
    
    /**
     * Convenience method to add vital signs entry.
     * 
     * @param vitalSign Map containing vital sign data (e.g., {"bloodPressure": "120/80", "heartRate": 72})
     */
    public void addVitalSign(Map<String, Object> vitalSign) {
        if (this.vitalSigns == null) {
            this.vitalSigns = new ArrayList<>();
        }
        this.vitalSigns.add(vitalSign);
    }
    
    /**
     * Convenience method to add metadata entry.
     * 
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    @Override
    public String toString() {
        return "PatientNote{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", timestamp=" + timestamp +
                ", noteType='" + noteType + '\'' +
                ", content='" + (content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
                ", diagnoses=" + diagnoses +
                '}';
    }
}
