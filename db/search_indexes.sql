-- Purpose: Add search optimization indexes
-- Description: Adds indexes on frequently searched columns to improve LIKE query performance


USE hms;

-- Add indexes on Patient table for search optimization
CREATE INDEX idx_patient_firstname ON Patient(FirstName);
CREATE INDEX idx_patient_lastname ON Patient(LastName);
CREATE INDEX idx_patient_phone ON Patient(PhoneNumber);
CREATE INDEX idx_patient_email ON Patient(Email);

-- Add indexes on Doctor table for search optimization
CREATE INDEX idx_doctor_firstname ON Doctor(FirstName);
CREATE INDEX idx_doctor_lastname ON Doctor(LastName);
CREATE INDEX idx_doctor_phone ON Doctor(PhoneNumber);
CREATE INDEX idx_doctor_email ON Doctor(Email);

-- Verify indexes were created
SHOW INDEX FROM Patient WHERE Key_name LIKE 'idx_patient%';
SHOW INDEX FROM Doctor WHERE Key_name LIKE 'idx_doctor%';
