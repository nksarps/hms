package com.nks.hms.service;

import com.nks.hms.model.PatientNote;
import com.nks.hms.repository.IPatientNoteRepository;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for patient note operations.
 * 
 * <p>Provides business logic layer between controllers and repository.
 * Could include validation, caching, or additional processing.
 */
public class PatientNoteService implements IPatientNoteService {
    private final IPatientNoteRepository repository;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param repository The patient note repository
     */
    public PatientNoteService(IPatientNoteRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public ObjectId createNote(PatientNote note) {
        // Could add validation here
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Note content cannot be empty");
        }
        
        if (note.getPatientId() <= 0) {
            throw new IllegalArgumentException("Invalid patient ID");
        }
        
        if (note.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID");
        }
        
        return repository.save(note);
    }
    
    @Override
    public Optional<PatientNote> getNoteById(ObjectId id) {
        return repository.findById(id);
    }
    
    @Override
    public List<PatientNote> getNotesByPatientId(int patientId) {
        return repository.findByPatientId(patientId);
    }
    
    @Override
    public List<PatientNote> getNotesByDoctorId(int doctorId) {
        return repository.findByDoctorId(doctorId);
    }
    
    @Override
    public List<PatientNote> getNotesByPatientAndDoctor(int patientId, int doctorId) {
        return repository.findByPatientAndDoctor(patientId, doctorId);
    }
    
    @Override
    public List<PatientNote> searchNotes(String searchText) {
        return repository.searchNoteContent(searchText);
    }
    
    @Override
    public List<PatientNote> getNotesByType(String noteType) {
        return repository.findByNoteType(noteType);
    }
    
    @Override
    public List<PatientNote> getAllNotes(int limit) {
        return repository.findAll(limit);
    }
    
    @Override
    public boolean updateNote(PatientNote note) {
        if (note.getId() == null) {
            throw new IllegalArgumentException("Cannot update note without ID");
        }
        
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Note content cannot be empty");
        }
        
        return repository.update(note);
    }
    
    @Override
    public boolean deleteNote(ObjectId id) {
        return repository.delete(id);
    }
    
    @Override
    public long deleteNotesByPatientId(int patientId) {
        return repository.deleteByPatientId(patientId);
    }
}
