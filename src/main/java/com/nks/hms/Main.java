package com.nks.hms;

import com.nks.hms.controller.AppointmentController;
import com.nks.hms.controller.DoctorController;
import com.nks.hms.controller.MedicalInventoryController;
import com.nks.hms.controller.PatientController;
import com.nks.hms.controller.PatientFeedbackController;
import com.nks.hms.controller.PrescriptionController;
import com.nks.hms.factory.ServiceFactory;
import com.nks.hms.service.IAppointmentService;
import com.nks.hms.service.IDoctorService;
import com.nks.hms.service.IMedicalInventoryService;
import com.nks.hms.service.IPatientFeedbackService;
import com.nks.hms.service.IPatientService;
import com.nks.hms.service.IPrescriptionService;
import com.nks.hms.ui.AppointmentTabBuilder;
import com.nks.hms.ui.DoctorTabBuilder;
import com.nks.hms.ui.MedicalInventoryTabBuilder;
import com.nks.hms.ui.PatientFeedbackTabBuilder;
import com.nks.hms.ui.PatientTabBuilder;
import com.nks.hms.ui.PrescriptionTabBuilder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for the Hospital Management System.
 * 
 * <p><b>Fully SOLID Compliant:</b>
 * <ul>
 *   <li><b>Single Responsibility:</b> Only handles JavaFX lifecycle and tab composition</li>
 *   <li><b>Open/Closed:</b> New tabs can be added via builders without modifying Main</li>
 *   <li><b>Liskov Substitution:</b> Uses interfaces - any implementation can be substituted</li>
 *   <li><b>Interface Segregation:</b> Depends only on specific service interfaces</li>
 *   <li><b>Dependency Inversion:</b> Uses ServiceFactory abstraction, no concrete dependencies</li>
 * </ul>
 * 
 * <p>Architecture layers:
 * <ul>
 *   <li>UI layer: TabBuilders + Controllers</li>
 *   <li>Service layer: PatientService, DoctorService</li>
 *   <li>Repository layer: PatientRepository, DoctorRepository</li>
 *   <li>Model layer: Patient, Doctor, VisitHistory POJOs</li>
 *   <li>Factory layer: ServiceFactory for dependency creation</li>
 * </ul>
 */
public class Main extends Application {
    
    // Dependencies created via factory (Dependency Inversion Principle)
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    private final IAppointmentService appointmentService;
    private final IPrescriptionService prescriptionService;
    private final IMedicalInventoryService inventoryService;
    private final IPatientFeedbackService feedbackService;
    private final PatientController patientController;
    private final DoctorController doctorController;
    private final AppointmentController appointmentController;
    private final PrescriptionController prescriptionController;
    private final MedicalInventoryController inventoryController;
    private final PatientFeedbackController feedbackController;
    
    /**
     * Default constructor that sets up dependencies via factory.
     * Uses ServiceFactory abstraction instead of creating concrete instances.
     */
    public Main() {
        // All dependencies created through factory - no concrete instantiation
        this.patientService = ServiceFactory.createPatientService();
        this.doctorService = ServiceFactory.createDoctorService();
        this.appointmentService = ServiceFactory.createAppointmentService();
        this.prescriptionService = ServiceFactory.createPrescriptionService();
        this.inventoryService = ServiceFactory.createMedicalInventoryService();
        this.feedbackService = ServiceFactory.createPatientFeedbackService();
        this.patientController = ServiceFactory.createPatientController(patientService);
        this.doctorController = ServiceFactory.createDoctorController(doctorService);
        this.appointmentController = ServiceFactory.createAppointmentController(appointmentService, patientService, doctorService);
        this.prescriptionController = ServiceFactory.createPrescriptionController(prescriptionService, patientService, doctorService);
        this.inventoryController = ServiceFactory.createMedicalInventoryController(inventoryService);
        this.feedbackController = ServiceFactory.createPatientFeedbackController(feedbackService);
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes and displays the main application window.
     * Delegates tab construction to builder classes (Single Responsibility).
     * 
     * @param stage The primary stage provided by JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Hospital Management - Data Access");
        TabPane tabs = new TabPane();
        
        // Use builders to construct tabs (Open/Closed Principle)
        tabs.getTabs().add(new PatientTabBuilder(patientController).build());
        tabs.getTabs().add(new DoctorTabBuilder(doctorController).build());
        tabs.getTabs().add(new PrescriptionTabBuilder(prescriptionController).build());
        tabs.getTabs().add(new AppointmentTabBuilder(appointmentController).build());
        tabs.getTabs().add(new MedicalInventoryTabBuilder(inventoryController).build());
        tabs.getTabs().add(new PatientFeedbackTabBuilder(feedbackController).build());
        
        stage.setScene(new Scene(tabs, 1200, 760));
        stage.show();
    }
}
