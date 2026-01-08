package com.nks.hms.factory;

import com.nks.hms.controller.AppointmentController;
import com.nks.hms.controller.DoctorController;
import com.nks.hms.controller.PatientController;
import com.nks.hms.repository.AppointmentRepository;
import com.nks.hms.repository.DoctorRepository;
import com.nks.hms.repository.IAppointmentRepository;
import com.nks.hms.repository.IDoctorRepository;
import com.nks.hms.repository.IPatientRepository;
import com.nks.hms.repository.PatientRepository;
import com.nks.hms.service.AppointmentService;
import com.nks.hms.service.DoctorService;
import com.nks.hms.service.IAppointmentService;
import com.nks.hms.service.IDoctorService;
import com.nks.hms.service.IPatientService;
import com.nks.hms.service.PatientService;
import com.nks.hms.validation.AppointmentValidator;
import com.nks.hms.validation.DoctorValidator;
import com.nks.hms.validation.IValidator;
import com.nks.hms.validation.PatientValidator;
import com.nks.hms.model.Appointment;
import com.nks.hms.model.Doctor;
import com.nks.hms.model.Patient;

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
    
    private static IValidator<Patient> createPatientValidator() {
        return new PatientValidator();
    }
    
    private static IValidator<Doctor> createDoctorValidator() {
        return new DoctorValidator();
    }
    
    private static IValidator<Appointment> createAppointmentValidator() {
        return new AppointmentValidator();
    }
}
