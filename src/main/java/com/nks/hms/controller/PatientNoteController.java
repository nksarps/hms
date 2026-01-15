package com.nks.hms.controller;

import com.nks.hms.model.Doctor;
import com.nks.hms.model.Patient;
import com.nks.hms.model.PatientNote;
import com.nks.hms.service.IDoctorService;
import com.nks.hms.service.IPatientNoteService;
import com.nks.hms.service.IPatientService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.bson.types.ObjectId;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for patient notes management UI.
 * 
 * <p>Handles interaction between the JavaFX UI and the patient note service layer.
 * Demonstrates NoSQL integration in a JavaFX application.
 */
public class PatientNoteController {
    private final IPatientNoteService noteService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param noteService Service for patient note operations
     * @param patientService Service for patient operations
     * @param doctorService Service for doctor operations
     */
    public PatientNoteController(IPatientNoteService noteService, 
                                 IPatientService patientService,
                                 IDoctorService doctorService) {
        this.noteService = noteService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }
    
    /**
     * Loads all patients into a ComboBox for selection.
     * 
     * @param patientCombo The ComboBox to populate
     * @param feedback Label for user feedback
     */
    public void loadPatients(ComboBox<Patient> patientCombo, Label feedback) {
        try {
            List<Patient> patients = patientService.searchPatients("", 1000, 0, "All");
            patientCombo.setItems(FXCollections.observableArrayList(patients));
            
            // Custom display for patient ComboBox
            patientCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Patient patient, boolean empty) {
                    super.updateItem(patient, empty);
                    if (empty || patient == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s %s (ID: %d)", 
                            patient.getFirstName(), patient.getLastName(), patient.getId()));
                    }
                }
            });
            
            patientCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Patient patient, boolean empty) {
                    super.updateItem(patient, empty);
                    if (empty || patient == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s %s (ID: %d)", 
                            patient.getFirstName(), patient.getLastName(), patient.getId()));
                    }
                }
            });
            
        } catch (Exception e) {
            feedback.setText("Error loading patients: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Loads all doctors into a ComboBox for selection.
     * 
     * @param doctorCombo The ComboBox to populate
     * @param feedback Label for user feedback
     */
    public void loadDoctors(ComboBox<Doctor> doctorCombo, Label feedback) {
        try {
            List<Doctor> doctors = doctorService.searchDoctors("", 1000, 0, "All");
            doctorCombo.setItems(FXCollections.observableArrayList(doctors));
            
            // Custom display for doctor ComboBox
            doctorCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Doctor doctor, boolean empty) {
                    super.updateItem(doctor, empty);
                    if (empty || doctor == null) {
                        setText(null);
                    } else {
                        setText(String.format("Dr. %s %s (ID: %d)", 
                            doctor.getFirstName(), doctor.getLastName(), doctor.getId()));
                    }
                }
            });
            
            doctorCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Doctor doctor, boolean empty) {
                    super.updateItem(doctor, empty);
                    if (empty || doctor == null) {
                        setText(null);
                    } else {
                        setText(String.format("Dr. %s %s (ID: %d)", 
                            doctor.getFirstName(), doctor.getLastName(), doctor.getId()));
                    }
                }
            });
            
        } catch (Exception e) {
            feedback.setText("Error loading doctors: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Loads patient notes into a ListView based on selected filters.
     * 
     * @param notesList ListView to populate
     * @param patientCombo Selected patient (can be null for all)
     * @param doctorCombo Selected doctor (can be null for all)
     * @param searchField Search text (can be empty)
     * @param feedback Label for user feedback
     */
    public void loadNotes(ListView<PatientNote> notesList, 
                         ComboBox<Patient> patientCombo,
                         ComboBox<Doctor> doctorCombo,
                         TextField searchField,
                         Label feedback) {
        try {
            List<PatientNote> notes;
            Patient selectedPatient = patientCombo.getValue();
            Doctor selectedDoctor = doctorCombo.getValue();
            String searchText = searchField.getText().trim();
            
            // Determine which query to use based on selections
            if (!searchText.isEmpty()) {
                notes = noteService.searchNotes(searchText);
            } else if (selectedPatient != null && selectedDoctor != null) {
                notes = noteService.getNotesByPatientAndDoctor(selectedPatient.getId(), selectedDoctor.getId());
            } else if (selectedPatient != null) {
                notes = noteService.getNotesByPatientId(selectedPatient.getId());
            } else if (selectedDoctor != null) {
                notes = noteService.getNotesByDoctorId(selectedDoctor.getId());
            } else {
                notes = noteService.getAllNotes(100); // Limit to 100 most recent
            }
            
            notesList.setItems(FXCollections.observableArrayList(notes));
            
            // Custom cell factory for displaying notes
            notesList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(PatientNote note, boolean empty) {
                    super.updateItem(note, empty);
                    if (empty || note == null) {
                        setText(null);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        String preview = note.getContent().length() > 50 
                            ? note.getContent().substring(0, 50) + "..." 
                            : note.getContent();
                        setText(String.format("[%s] %s - Patient:%d Doctor:%d\n%s",
                            note.getTimestamp().format(formatter),
                            note.getNoteType(),
                            note.getPatientId(),
                            note.getDoctorId(),
                            preview));
                    }
                }
            });
            
            feedback.setText(String.format("Loaded %d note(s)", notes.size()));
            feedback.setStyle("-fx-text-fill: green;");
            
        } catch (Exception e) {
            feedback.setText("Error loading notes: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a new patient note.
     * 
     * @param patientCombo Selected patient
     * @param doctorCombo Selected doctor
     * @param noteTypeCombo Selected note type
     * @param contentArea Note content
     * @param notesList ListView to refresh
     * @param searchField Current search text
     * @param feedback Label for user feedback
     */
    public void saveNote(ComboBox<Patient> patientCombo,
                        ComboBox<Doctor> doctorCombo,
                        ComboBox<String> noteTypeCombo,
                        TextArea contentArea,
                        ListView<PatientNote> notesList,
                        TextField searchField,
                        Label feedback) {
        try {
            Patient selectedPatient = patientCombo.getValue();
            Doctor selectedDoctor = doctorCombo.getValue();
            String noteType = noteTypeCombo.getValue();
            String content = contentArea.getText();
            
            // Validation
            if (selectedPatient == null) {
                feedback.setText("Please select a patient");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedDoctor == null) {
                feedback.setText("Please select a doctor");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (noteType == null || noteType.isEmpty()) {
                feedback.setText("Please select a note type");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (content == null || content.trim().isEmpty()) {
                feedback.setText("Please enter note content");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Create and save note
            PatientNote note = new PatientNote(
                selectedPatient.getId(),
                selectedDoctor.getId(),
                noteType,
                content.trim()
            );
            
            ObjectId noteId = noteService.createNote(note);
            
            feedback.setText("Note saved successfully with ID: " + noteId.toHexString());
            feedback.setStyle("-fx-text-fill: green;");
            
            // Clear form
            contentArea.clear();
            noteTypeCombo.setValue(null);
            
            // Reload notes list
            loadNotes(notesList, patientCombo, doctorCombo, searchField, feedback);
            
        } catch (Exception e) {
            feedback.setText("Error saving note: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a selected note.
     * 
     * @param notesList ListView containing notes
     * @param patientCombo Current patient filter
     * @param doctorCombo Current doctor filter
     * @param searchField Current search text
     * @param feedback Label for user feedback
     */
    public void deleteNote(ListView<PatientNote> notesList,
                          ComboBox<Patient> patientCombo,
                          ComboBox<Doctor> doctorCombo,
                          TextField searchField,
                          Label feedback) {
        try {
            PatientNote selectedNote = notesList.getSelectionModel().getSelectedItem();
            
            if (selectedNote == null) {
                feedback.setText("Please select a note to delete");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Note");
            alert.setHeaderText("Are you sure you want to delete this note?");
            alert.setContentText("This action cannot be undone.");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                boolean deleted = noteService.deleteNote(selectedNote.getId());
                
                if (deleted) {
                    feedback.setText("Note deleted successfully");
                    feedback.setStyle("-fx-text-fill: green;");
                    
                    // Reload notes list
                    loadNotes(notesList, patientCombo, doctorCombo, searchField, feedback);
                } else {
                    feedback.setText("Failed to delete note");
                    feedback.setStyle("-fx-text-fill: red;");
                }
            }
            
        } catch (Exception e) {
            feedback.setText("Error deleting note: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Populates the form with data from a selected note for editing.
     * 
     * @param note The selected note
     * @param patientCombo Patient selection combo
     * @param doctorCombo Doctor selection combo
     * @param noteTypeCombo Note type combo
     * @param contentArea Content text area
     */
    public void populateFormForEdit(PatientNote note,
                                    ComboBox<Patient> patientCombo,
                                    ComboBox<Doctor> doctorCombo,
                                    ComboBox<String> noteTypeCombo,
                                    TextArea contentArea) {
        if (note == null) {
            clearForm(patientCombo, doctorCombo, noteTypeCombo, contentArea);
            return;
        }
        
        try {
            // Find and select the patient
            for (Patient patient : patientCombo.getItems()) {
                if (patient.getId() == note.getPatientId()) {
                    patientCombo.setValue(patient);
                    break;
                }
            }
            
            // Find and select the doctor
            for (Doctor doctor : doctorCombo.getItems()) {
                if (doctor.getId() == note.getDoctorId()) {
                    doctorCombo.setValue(doctor);
                    break;
                }
            }
            
            // Set note type and content
            noteTypeCombo.setValue(note.getNoteType());
            contentArea.setText(note.getContent());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Updates an existing note with new information.
     * 
     * @param selectedNote The note to update
     * @param patientCombo Selected patient
     * @param doctorCombo Selected doctor
     * @param noteTypeCombo Selected note type
     * @param contentArea Note content
     * @param notesList ListView to refresh
     * @param searchField Current search text
     * @param feedback Label for user feedback
     */
    public void updateNote(PatientNote selectedNote,
                          ComboBox<Patient> patientCombo,
                          ComboBox<Doctor> doctorCombo,
                          ComboBox<String> noteTypeCombo,
                          TextArea contentArea,
                          ListView<PatientNote> notesList,
                          TextField searchField,
                          Label feedback) {
        try {
            if (selectedNote == null) {
                feedback.setText("Please select a note to update");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Patient selectedPatient = patientCombo.getValue();
            Doctor selectedDoctor = doctorCombo.getValue();
            String noteType = noteTypeCombo.getValue();
            String content = contentArea.getText();
            
            // Validation
            if (selectedPatient == null) {
                feedback.setText("Please select a patient");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedDoctor == null) {
                feedback.setText("Please select a doctor");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (noteType == null || noteType.isEmpty()) {
                feedback.setText("Please select a note type");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (content == null || content.trim().isEmpty()) {
                feedback.setText("Please enter note content");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Update the note fields
            selectedNote.setPatientId(selectedPatient.getId());
            selectedNote.setDoctorId(selectedDoctor.getId());
            selectedNote.setNoteType(noteType);
            selectedNote.setContent(content.trim());
            
            boolean updated = noteService.updateNote(selectedNote);
            
            if (updated) {
                feedback.setText("Note updated successfully");
                feedback.setStyle("-fx-text-fill: green;");
                
                // Clear form
                clearForm(patientCombo, doctorCombo, noteTypeCombo, contentArea);
                
                // Reload notes list
                loadNotes(notesList, patientCombo, doctorCombo, searchField, feedback);
            } else {
                feedback.setText("Failed to update note");
                feedback.setStyle("-fx-text-fill: red;");
            }
            
        } catch (Exception e) {
            feedback.setText("Error updating note: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Clears all form fields.
     * 
     * @param patientCombo Patient selection combo
     * @param doctorCombo Doctor selection combo
     * @param noteTypeCombo Note type combo
     * @param contentArea Content text area
     */
    public void clearForm(ComboBox<Patient> patientCombo,
                         ComboBox<Doctor> doctorCombo,
                         ComboBox<String> noteTypeCombo,
                         TextArea contentArea) {
        patientCombo.setValue(null);
        doctorCombo.setValue(null);
        noteTypeCombo.setValue(null);
        contentArea.clear();
    }
    
    // ==================== TableView with Pagination Methods ====================
    
    /**
     * Loads patient notes into a TableView with pagination.
     * 
     * @param table TableView to populate
     * @param pagination Pagination control
     * @param searchText Search text (can be empty)
     * @param selectedPatient Selected patient filter (can be null)
     * @param selectedDoctor Selected doctor filter (can be null)
     * @param feedback Label for user feedback
     */
    public void loadNotes(TableView<PatientNote> table,
                         Pagination pagination,
                         String searchText,
                         Patient selectedPatient,
                         Doctor selectedDoctor,
                         Label feedback) {
        try {
            List<PatientNote> notes;
            
            // Determine which query to use based on selections
            if (searchText != null && !searchText.trim().isEmpty()) {
                notes = noteService.searchNotes(searchText.trim());
            } else if (selectedPatient != null && selectedDoctor != null) {
                notes = noteService.getNotesByPatientAndDoctor(selectedPatient.getId(), selectedDoctor.getId());
            } else if (selectedPatient != null) {
                notes = noteService.getNotesByPatientId(selectedPatient.getId());
            } else if (selectedDoctor != null) {
                notes = noteService.getNotesByDoctorId(selectedDoctor.getId());
            } else {
                notes = noteService.getAllNotes(1000); // Get all notes
            }
            
            // Update pagination
            int pageSize = 20; // Items per page
            int totalPages = (int) Math.ceil((double) notes.size() / pageSize);
            pagination.setPageCount(Math.max(1, totalPages));
            
            // Get current page data
            int currentPage = pagination.getCurrentPageIndex();
            int fromIndex = currentPage * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, notes.size());
            
            List<PatientNote> pageData = notes.subList(fromIndex, toIndex);
            table.setItems(FXCollections.observableArrayList(pageData));
            
            feedback.setText(String.format("Loaded %d note(s)", notes.size()));
            feedback.setStyle("-fx-text-fill: green;");
            
        } catch (Exception e) {
            feedback.setText("Error loading notes: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a new patient note (TableView version).
     * 
     * @param patientCombo Selected patient
     * @param doctorCombo Selected doctor
     * @param noteTypeCombo Selected note type
     * @param contentArea Note content
     * @param table TableView to refresh
     * @param pagination Pagination control
     * @param searchField Current search text
     * @param feedback Label for user feedback
     */
    public void saveNote(ComboBox<Patient> patientCombo,
                        ComboBox<Doctor> doctorCombo,
                        ComboBox<String> noteTypeCombo,
                        TextArea contentArea,
                        TableView<PatientNote> table,
                        Pagination pagination,
                        TextField searchField,
                        Label feedback) {
        try {
            Patient selectedPatient = patientCombo.getValue();
            Doctor selectedDoctor = doctorCombo.getValue();
            String noteType = noteTypeCombo.getValue();
            String content = contentArea.getText();
            
            // Validation
            if (selectedPatient == null) {
                feedback.setText("Please select a patient");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedDoctor == null) {
                feedback.setText("Please select a doctor");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (noteType == null || noteType.isEmpty()) {
                feedback.setText("Please select a note type");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (content == null || content.trim().isEmpty()) {
                feedback.setText("Please enter note content");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Create and save note
            PatientNote note = new PatientNote(
                selectedPatient.getId(),
                selectedDoctor.getId(),
                noteType,
                content.trim()
            );
            
            ObjectId noteId = noteService.createNote(note);
            
            feedback.setText("Note saved successfully with ID: " + noteId.toHexString());
            feedback.setStyle("-fx-text-fill: green;");
            
            // Clear form
            clearForm(patientCombo, doctorCombo, noteTypeCombo, contentArea);
            
            // Reload notes table
            loadNotes(table, pagination, searchField.getText(), null, null, feedback);
            
        } catch (Exception e) {
            feedback.setText("Error saving note: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Updates an existing note (TableView version).
     * 
     * @param selectedNote The note to update
     * @param patientCombo Selected patient
     * @param doctorCombo Selected doctor
     * @param noteTypeCombo Selected note type
     * @param contentArea Note content
     * @param table TableView to refresh
     * @param pagination Pagination control
     * @param searchField Current search text
     * @param feedback Label for user feedback
     */
    public void updateNote(PatientNote selectedNote,
                          ComboBox<Patient> patientCombo,
                          ComboBox<Doctor> doctorCombo,
                          ComboBox<String> noteTypeCombo,
                          TextArea contentArea,
                          TableView<PatientNote> table,
                          Pagination pagination,
                          TextField searchField,
                          Label feedback) {
        try {
            if (selectedNote == null) {
                feedback.setText("Please select a note to update");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Patient selectedPatient = patientCombo.getValue();
            Doctor selectedDoctor = doctorCombo.getValue();
            String noteType = noteTypeCombo.getValue();
            String content = contentArea.getText();
            
            // Validation
            if (selectedPatient == null) {
                feedback.setText("Please select a patient");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedDoctor == null) {
                feedback.setText("Please select a doctor");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (noteType == null || noteType.isEmpty()) {
                feedback.setText("Please select a note type");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (content == null || content.trim().isEmpty()) {
                feedback.setText("Please enter note content");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Update the note fields
            selectedNote.setPatientId(selectedPatient.getId());
            selectedNote.setDoctorId(selectedDoctor.getId());
            selectedNote.setNoteType(noteType);
            selectedNote.setContent(content.trim());
            
            boolean updated = noteService.updateNote(selectedNote);
            
            if (updated) {
                feedback.setText("Note updated successfully");
                feedback.setStyle("-fx-text-fill: green;");
                
                // Clear form
                clearForm(patientCombo, doctorCombo, noteTypeCombo, contentArea);
                
                // Reload notes table
                loadNotes(table, pagination, searchField.getText(), null, null, feedback);
            } else {
                feedback.setText("Failed to update note");
                feedback.setStyle("-fx-text-fill: red;");
            }
            
        } catch (Exception e) {
            feedback.setText("Error updating note: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a selected note (TableView version).
     * 
     * @param table TableView containing notes
     * @param pagination Pagination control
     * @param searchField Current search text
     * @param feedback Label for user feedback
     */
    public void deleteNote(TableView<PatientNote> table,
                          Pagination pagination,
                          TextField searchField,
                          Label feedback) {
        try {
            PatientNote selectedNote = table.getSelectionModel().getSelectedItem();
            
            if (selectedNote == null) {
                feedback.setText("Please select a note to delete");
                feedback.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Note");
            alert.setHeaderText("Are you sure you want to delete this note?");
            alert.setContentText("This action cannot be undone.");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                boolean deleted = noteService.deleteNote(selectedNote.getId());
                
                if (deleted) {
                    feedback.setText("Note deleted successfully");
                    feedback.setStyle("-fx-text-fill: green;");
                    
                    // Reload notes table
                    loadNotes(table, pagination, searchField.getText(), null, null, feedback);
                } else {
                    feedback.setText("Failed to delete note");
                    feedback.setStyle("-fx-text-fill: red;");
                }
            }
            
        } catch (Exception e) {
            feedback.setText("Error deleting note: " + e.getMessage());
            feedback.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}
