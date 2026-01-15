package com.nks.hms.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.nks.hms.model.PatientNote;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * MongoDB implementation of PatientNoteRepository.
 * 
 * <p>Demonstrates NoSQL capabilities:
 * <ul>
 *   <li>Flexible document storage without rigid schema</li>
 *   <li>Full-text search capabilities</li>
 *   <li>Nested data structures (vital signs, diagnoses)</li>
 *   <li>No join operations needed</li>
 * </ul>
 */
public class PatientNoteRepository implements IPatientNoteRepository {
    private final MongoCollection<Document> collection;
    
    /**
     * Constructor that initializes the MongoDB collection.
     * Creates text index on content field for full-text search.
     * 
     * @param database MongoDB database instance
     */
    public PatientNoteRepository(MongoDatabase database) {
        this.collection = database.getCollection("patient_notes");
        
        // Create text index on content field for full-text search
        try {
            collection.createIndex(Indexes.text("content"));
        } catch (Exception e) {
            // Index might already exist, ignore
        }
    }
    
    @Override
    public ObjectId save(PatientNote note) {
        Document doc = toDocument(note);
        
        if (note.getId() == null) {
            // Insert new document
            collection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Update existing document
            collection.replaceOne(Filters.eq("_id", note.getId()), doc);
            return note.getId();
        }
    }
    
    @Override
    public Optional<PatientNote> findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc != null ? Optional.of(toPatientNote(doc)) : Optional.empty();
    }
    
    @Override
    public List<PatientNote> findByPatientId(int patientId) {
        List<PatientNote> notes = new ArrayList<>();
        collection.find(Filters.eq("patientId", patientId))
                .sort(new Document("timestamp", -1)) // Most recent first
                .into(new ArrayList<>())
                .forEach(doc -> notes.add(toPatientNote(doc)));
        return notes;
    }
    
    @Override
    public List<PatientNote> findByDoctorId(int doctorId) {
        List<PatientNote> notes = new ArrayList<>();
        collection.find(Filters.eq("doctorId", doctorId))
                .sort(new Document("timestamp", -1))
                .into(new ArrayList<>())
                .forEach(doc -> notes.add(toPatientNote(doc)));
        return notes;
    }
    
    @Override
    public List<PatientNote> findByPatientAndDoctor(int patientId, int doctorId) {
        List<PatientNote> notes = new ArrayList<>();
        collection.find(Filters.and(
                Filters.eq("patientId", patientId),
                Filters.eq("doctorId", doctorId)
        ))
                .sort(new Document("timestamp", -1))
                .into(new ArrayList<>())
                .forEach(doc -> notes.add(toPatientNote(doc)));
        return notes;
    }
    
    @Override
    public List<PatientNote> searchNoteContent(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return findAll(100);
        }
        
        List<PatientNote> notes = new ArrayList<>();
        // MongoDB full-text search
        collection.find(Filters.text(searchText))
                .sort(new Document("timestamp", -1))
                .into(new ArrayList<>())
                .forEach(doc -> notes.add(toPatientNote(doc)));
        return notes;
    }
    
    @Override
    public List<PatientNote> findByNoteType(String noteType) {
        List<PatientNote> notes = new ArrayList<>();
        collection.find(Filters.eq("noteType", noteType))
                .sort(new Document("timestamp", -1))
                .into(new ArrayList<>())
                .forEach(doc -> notes.add(toPatientNote(doc)));
        return notes;
    }
    
    @Override
    public List<PatientNote> findAll(int limit) {
        List<PatientNote> notes = new ArrayList<>();
        var query = collection.find().sort(new Document("timestamp", -1));
        
        if (limit > 0) {
            query = query.limit(limit);
        }
        
        query.into(new ArrayList<>())
                .forEach(doc -> notes.add(toPatientNote(doc)));
        return notes;
    }
    
    @Override
    public boolean update(PatientNote note) {
        if (note.getId() == null) {
            return false;
        }
        
        Document doc = toDocument(note);
        var result = collection.replaceOne(Filters.eq("_id", note.getId()), doc);
        return result.getModifiedCount() > 0;
    }
    
    @Override
    public boolean delete(ObjectId id) {
        var result = collection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    @Override
    public long deleteByPatientId(int patientId) {
        var result = collection.deleteMany(Filters.eq("patientId", patientId));
        return result.getDeletedCount();
    }
    
    /**
     * Converts PatientNote object to MongoDB Document.
     * Demonstrates flexible schema - different notes can have different fields.
     */
    private Document toDocument(PatientNote note) {
        Document doc = new Document();
        
        if (note.getId() != null) {
            doc.append("_id", note.getId());
        }
        
        doc.append("patientId", note.getPatientId())
           .append("doctorId", note.getDoctorId())
           .append("timestamp", Date.from(note.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
           .append("noteType", note.getNoteType())
           .append("content", note.getContent());
        
        // Optional fields - demonstrates schema flexibility
        if (note.getDiagnoses() != null && !note.getDiagnoses().isEmpty()) {
            doc.append("diagnoses", note.getDiagnoses());
        }
        
        if (note.getVitalSigns() != null && !note.getVitalSigns().isEmpty()) {
            doc.append("vitalSigns", note.getVitalSigns());
        }
        
        if (note.getMetadata() != null && !note.getMetadata().isEmpty()) {
            doc.append("metadata", note.getMetadata());
        }
        
        return doc;
    }
    
    /**
     * Converts MongoDB Document to PatientNote object.
     */
    @SuppressWarnings("unchecked")
    private PatientNote toPatientNote(Document doc) {
        PatientNote note = new PatientNote();
        
        note.setId(doc.getObjectId("_id"));
        note.setPatientId(doc.getInteger("patientId"));
        note.setDoctorId(doc.getInteger("doctorId"));
        
        Date timestamp = doc.getDate("timestamp");
        if (timestamp != null) {
            note.setTimestamp(timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        note.setNoteType(doc.getString("noteType"));
        note.setContent(doc.getString("content"));
        
        // Handle optional nested fields
        List<String> diagnoses = (List<String>) doc.get("diagnoses");
        if (diagnoses != null) {
            note.setDiagnoses(diagnoses);
        }
        
        List<Map<String, Object>> vitalSigns = (List<Map<String, Object>>) doc.get("vitalSigns");
        if (vitalSigns != null) {
            note.setVitalSigns(vitalSigns);
        }
        
        Map<String, Object> metadata = (Map<String, Object>) doc.get("metadata");
        if (metadata != null) {
            note.setMetadata(metadata);
        }
        
        return note;
    }
}
