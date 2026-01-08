package com.nks.hms.repository;

import com.nks.hms.model.Doctor;

/**
 * Doctor-specific repository interface extending base repository operations.
 * Currently uses only base CRUD operations but provides room for doctor-specific
 * methods in the future (e.g., finding by department, specialization, etc.).
 */
public interface IDoctorRepository extends IRepository<Doctor> {
    // Currently no doctor-specific methods beyond base CRUD
    // This interface provides room for future extensions like:
    // - List<Doctor> findByDepartment(int departmentId) throws SQLException;
    // - List<Doctor> findBySpecialization(String specialization) throws SQLException;
}
