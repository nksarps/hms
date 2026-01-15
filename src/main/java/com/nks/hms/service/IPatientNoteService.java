package com.nks.hms.service;

import com.nks.hms.model.PatientNote;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for patient note business logic.
 * 
 * <p>Provides high-level operations for managing patient notes
 * stored in MongoDB. Demonstrates NoSQL service layer patterns.
 */
public interface IPatientNoteService {
    
    /**
     * Creates a new patient note.
     * 
     * @param note The note to create
     * @return The ObjectId of the created note
     */
    ObjectId createNote(PatientNote note);
    
    /**
     * Retrieves a note by its ObjectId.
     * 
     * @param id The note's ObjectId
     * @return Optional containing the note if found
     */
    Optional<PatientNote> getNoteById(ObjectId id);
    
    /**
     * Retrieves all notes for a specific patient.
     * 
     * @param patientId The patient's ID
     * @return List of patient notes
     */
    List<PatientNote> getNotesByPatientId(int patientId);
    
    /**
     * Retrieves all notes created by a specific doctor.
     * 
     * @param doctorId The doctor's ID
     * @return List of patient notes
     */
    List<PatientNote> getNotesByDoctorId(int doctorId);
    
    /**
     * Retrieves notes for a specific patient-doctor combination.
     * 
     * @param patientId The patient's ID
     * @param doctorId The doctor's ID
     * @return List of matching notes
     */
    List<PatientNote> getNotesByPatientAndDoctor(int patientId, int doctorId);
    
    /**
     * Searches notes using full-text search on content.
     * 
     * @param searchText Text to search for
     * @return List of matching notes
     */
    List<PatientNote> searchNotes(String searchText);
    
    /**
     * Retrieves notes by type.
     * 
     * @param noteType The type of note
     * @return List of matching notes
     */
    List<PatientNote> getNotesByType(String noteType);
    
    /**
     * Retrieves all notes (limited).
     * 
     * @param limit Maximum number of notes (0 for all)
     * @return List of notes
     */
    List<PatientNote> getAllNotes(int limit);
    
    /**
     * Updates an existing note.
     * 
     * @param note The note with updated information
     * @return true if successful
     */
    boolean updateNote(PatientNote note);
    
    /**
     * Deletes a note by ObjectId.
     * 
     * @param id The note's ObjectId
     * @return true if successful
     */
    boolean deleteNote(ObjectId id);
    
    /**
     * Deletes all notes for a patient.
     * 
     * @param patientId The patient's ID
     * @return Number of notes deleted
     */
    long deleteNotesByPatientId(int patientId);
}
