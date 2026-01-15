package com.nks.hms.repository;

import com.nks.hms.model.PatientNote;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PatientNote NoSQL operations.
 * 
 * <p>Defines contract for MongoDB-based patient note storage.
 * Supports flexible querying and full-text search capabilities
 * that demonstrate NoSQL advantages over relational databases.
 */
public interface IPatientNoteRepository {
    
    /**
     * Saves a new patient note to MongoDB.
     * 
     * @param note The patient note to save
     * @return The ObjectId of the saved note
     */
    ObjectId save(PatientNote note);
    
    /**
     * Finds a patient note by its MongoDB ObjectId.
     * 
     * @param id The ObjectId to search for
     * @return Optional containing the note if found
     */
    Optional<PatientNote> findById(ObjectId id);
    
    /**
     * Finds all notes for a specific patient.
     * 
     * @param patientId The patient's ID from MySQL
     * @return List of patient notes
     */
    List<PatientNote> findByPatientId(int patientId);
    
    /**
     * Finds all notes created by a specific doctor.
     * 
     * @param doctorId The doctor's ID from MySQL
     * @return List of patient notes
     */
    List<PatientNote> findByDoctorId(int doctorId);
    
    /**
     * Finds notes by both patient and doctor.
     * 
     * @param patientId The patient's ID
     * @param doctorId The doctor's ID
     * @return List of matching patient notes
     */
    List<PatientNote> findByPatientAndDoctor(int patientId, int doctorId);
    
    /**
     * Searches note content using MongoDB full-text search.
     * Demonstrates NoSQL advantage over basic SQL TEXT search.
     * 
     * @param searchText Text to search for in note content
     * @return List of matching patient notes
     */
    List<PatientNote> searchNoteContent(String searchText);
    
    /**
     * Finds all notes of a specific type.
     * 
     * @param noteType The type of note (clinical, discharge, etc.)
     * @return List of matching notes
     */
    List<PatientNote> findByNoteType(String noteType);
    
    /**
     * Finds all notes (with optional limit).
     * 
     * @param limit Maximum number of notes to return (0 for all)
     * @return List of patient notes
     */
    List<PatientNote> findAll(int limit);
    
    /**
     * Updates an existing patient note.
     * 
     * @param note The note with updated information
     * @return true if update was successful
     */
    boolean update(PatientNote note);
    
    /**
     * Deletes a patient note by ObjectId.
     * 
     * @param id The ObjectId of the note to delete
     * @return true if deletion was successful
     */
    boolean delete(ObjectId id);
    
    /**
     * Deletes all notes for a specific patient.
     * 
     * @param patientId The patient's ID
     * @return Number of notes deleted
     */
    long deleteByPatientId(int patientId);
}
