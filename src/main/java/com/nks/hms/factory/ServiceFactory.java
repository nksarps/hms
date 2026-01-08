package com.nks.hms.factory;

import com.nks.hms.controller.AppointmentController;
import com.nks.hms.controller.DoctorController;
import com.nks.hms.controller.MedicalInventoryController;
import com.nks.hms.controller.PatientController;
import com.nks.hms.controller.PatientFeedbackController;
import com.nks.hms.controller.PrescriptionController;
import com.nks.hms.repository.AppointmentRepository;
import com.nks.hms.repository.DoctorRepository;
import com.nks.hms.repository.IAppointmentRepository;
import com.nks.hms.repository.IDoctorRepository;
import com.nks.hms.repository.IMedicalInventoryRepository;
import com.nks.hms.repository.IPatientFeedbackRepository;
import com.nks.hms.repository.IPatientRepository;
import com.nks.hms.repository.IPrescriptionRepository;
import com.nks.hms.repository.MedicalInventoryRepository;
import com.nks.hms.repository.PatientFeedbackRepository;
import com.nks.hms.repository.PatientRepository;
import com.nks.hms.repository.PrescriptionRepository;
import com.nks.hms.service.AppointmentService;
import com.nks.hms.service.DoctorService;
import com.nks.hms.service.IAppointmentService;
import com.nks.hms.service.IDoctorService;
import com.nks.hms.service.IMedicalInventoryService;
import com.nks.hms.service.IPatientFeedbackService;
import com.nks.hms.service.IPatientService;
import com.nks.hms.service.IPrescriptionService;
import com.nks.hms.service.MedicalInventoryService;
import com.nks.hms.service.PatientFeedbackService;
import com.nks.hms.service.PatientService;
import com.nks.hms.service.PrescriptionService;
import com.nks.hms.validation.AppointmentValidator;
import com.nks.hms.validation.DoctorValidator;
import com.nks.hms.validation.IValidator;
import com.nks.hms.validation.MedicalInventoryValidator;
import com.nks.hms.validation.PatientFeedbackValidator;
import com.nks.hms.validation.PatientValidator;
import com.nks.hms.validation.PrescriptionValidator;
import com.nks.hms.model.Appointment;
import com.nks.hms.model.Doctor;
import com.nks.hms.model.MedicalInventory;
import com.nks.hms.model.Patient;
import com.nks.hms.model.PatientFeedback;
import com.nks.hms.model.Prescription;

/**
 * Factory for creating service layer dependencies.
 * 
 * <p>Centralizes object creation to support Dependency Inversion Principle.
 * High-level modules depend on this factory instead of creating concrete instances.
 * 
 * <p>Benefits:
 * <ul>
 *   <li>Single point to change implementations</li>
 *   <li>Easy to swap implementations for testing or different environments</li>
 *   <li>Follows Open/Closed Principle - extend by adding factory methods</li>
 * </ul>
 * 
 * <p>In production, this could be replaced with a DI framework like Spring or Guice.
 */
public class ServiceFactory {
    
    /**
     * Creates a configured patient service with all dependencies.
     * 
     * @return Fully configured patient service
     */
    public static IPatientService createPatientService() {
        IPatientRepository repository = createPatientRepository();
        IValidator<Patient> validator = createPatientValidator();
        return new PatientService(repository, validator);
    }
    
    /**
     * Creates a configured doctor service with all dependencies.
     * 
     * @return Fully configured doctor service
     */
    public static IDoctorService createDoctorService() {
        IDoctorRepository repository = createDoctorRepository();
        IValidator<Doctor> validator = createDoctorValidator();
        return new DoctorService(repository, validator);
    }
    
    /**
     * Creates a patient controller with injected service.
     * 
     * @param patientService The patient service to inject
     * @return Configured patient controller
     */
    public static PatientController createPatientController(IPatientService patientService) {
        return new PatientController(patientService);
    }
    
    /**
     * Creates a configured appointment service with all dependencies.
     * 
     * @return Fully configured appointment service
     */
    public static IAppointmentService createAppointmentService() {
        IAppointmentRepository repository = createAppointmentRepository();
        IValidator<Appointment> validator = createAppointmentValidator();
        return new AppointmentService(repository, validator);
    }
    
    /**
     * Creates an appointment controller with injected services.
     * 
     * @param appointmentService The appointment service to inject
     * @param patientService The patient service to inject
     * @param doctorService The doctor service to inject
     * @return Configured appointment controller
     */
    public static AppointmentController createAppointmentController(IAppointmentService appointmentService,
                                                                    IPatientService patientService,
                                                                    IDoctorService doctorService) {
        return new AppointmentController(appointmentService, patientService, doctorService);
    }
    
    /**
     * Creates a doctor controller with injected service.
     * 
     * @param doctorService The doctor service to inject
     * @return Configured doctor controller
     */
    public static DoctorController createDoctorController(IDoctorService doctorService) {
        return new DoctorController(doctorService);
    }
    
    /**
     * Creates a configured prescription service with all dependencies.
     * 
     * @return Fully configured prescription service
     */
    public static IPrescriptionService createPrescriptionService() {
        IPrescriptionRepository repository = createPrescriptionRepository();
        IValidator<Prescription> validator = createPrescriptionValidator();
        return new PrescriptionService(repository, validator);
    }
    
    /**
     * Creates a prescription controller with injected services.
     * 
     * @param prescriptionService The prescription service to inject
     * @param patientService The patient service to inject
     * @param doctorService The doctor service to inject
     * @return Configured prescription controller
     */
    public static PrescriptionController createPrescriptionController(IPrescriptionService prescriptionService,
                                                                      IPatientService patientService,
                                                                      IDoctorService doctorService) {
        return new PrescriptionController(prescriptionService, patientService, doctorService);
    }
    
    /**
     * Creates a configured medical inventory service with all dependencies.
     * 
     * @return Fully configured medical inventory service
     */
    public static IMedicalInventoryService createMedicalInventoryService() {
        IMedicalInventoryRepository repository = createMedicalInventoryRepository();
        IValidator<MedicalInventory> validator = createMedicalInventoryValidator();
        return new MedicalInventoryService(repository, validator);
    }
    
    /**
     * Creates a medical inventory controller with injected service.
     * 
     * @param inventoryService The medical inventory service to inject
     * @return Configured medical inventory controller
     */
    public static MedicalInventoryController createMedicalInventoryController(IMedicalInventoryService inventoryService) {
        return new MedicalInventoryController(inventoryService);
    }
    
    /**
     * Creates a configured patient feedback service with all dependencies.
     * 
     * @return Fully configured patient feedback service
     */
    public static IPatientFeedbackService createPatientFeedbackService() {
        IPatientFeedbackRepository repository = createPatientFeedbackRepository();
        IValidator<PatientFeedback> validator = createPatientFeedbackValidator();
        return new PatientFeedbackService(repository, validator);
    }
    
    /**
     * Creates a patient feedback controller with injected service.
     * 
     * @param feedbackService The patient feedback service to inject
     * @return Configured patient feedback controller
     */
    public static PatientFeedbackController createPatientFeedbackController(IPatientFeedbackService feedbackService) {
        return new PatientFeedbackController(feedbackService);
    }
    
    // Private factory methods for internal dependencies
    
    private static IPatientRepository createPatientRepository() {
        return new PatientRepository();
    }
    
    private static IDoctorRepository createDoctorRepository() {
        return new DoctorRepository();
    }
    
    private static IAppointmentRepository createAppointmentRepository() {
        return new AppointmentRepository();
    }
    
    private static IPrescriptionRepository createPrescriptionRepository() {
        return new PrescriptionRepository();
    }
    
    private static IMedicalInventoryRepository createMedicalInventoryRepository() {
        return new MedicalInventoryRepository();
    }
    
    private static IPatientFeedbackRepository createPatientFeedbackRepository() {
        return new PatientFeedbackRepository();
    }
    
    private static IValidator<Patient> createPatientValidator() {
        return new PatientValidator();
    }
    
    private static IValidator<Doctor> createDoctorValidator() {
        return new DoctorValidator();
    }
    
    private static IValidator<Appointment> createAppointmentValidator() {
        return new AppointmentValidator();
    }
    
    private static IValidator<Prescription> createPrescriptionValidator() {
        return new PrescriptionValidator();
    }
    
    private static IValidator<MedicalInventory> createMedicalInventoryValidator() {
        return new MedicalInventoryValidator();
    }
    
    private static IValidator<PatientFeedback> createPatientFeedbackValidator() {
        return new PatientFeedbackValidator();
    }
}
