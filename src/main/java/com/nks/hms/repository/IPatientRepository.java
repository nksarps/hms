package com.nks.hms.repository;

import com.nks.hms.model.Patient;
import com.nks.hms.model.VisitHistory;
import java.sql.SQLException;
import java.util.List;

/**
 * Patient-specific repository interface extending base repository operations.
 * Adds patient-specific functionality like visit history retrieval.
 */
public interface IPatientRepository extends IRepository<Patient> {
    /**
     * Retrieves the visit history for a specific patient.
     * 
     * @param patientId The patient's unique identifier
     * @return List of visit history records (empty if patient has no visits)
     * @throws SQLException If database access fails
     */
    List<VisitHistory> getVisitHistory(int patientId) throws SQLException;
}
