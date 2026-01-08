-- Purpose: Sample queries for testing and data verification

USE hms;

-- Quick full-table views
SELECT * FROM Department;
SELECT * FROM Doctor;
SELECT * FROM Patient;
SELECT * FROM MedicalInventory;
SELECT * FROM Appointment;
SELECT * FROM Prescription;
SELECT * FROM PrescriptionItem;
SELECT * FROM PatientFeedback;

-- Helpful joined views
-- Appointments with patient + doctor
SELECT a.ID, a.AppointmentDate, a.Reason,
       p.FirstName AS PatientFirst, p.LastName AS PatientLast,
       d.FirstName AS DoctorFirst, d.LastName AS DoctorLast, dept.Name AS Department
FROM Appointment a
JOIN Patient p ON p.ID = a.PatientID
JOIN Doctor d ON d.ID = a.DoctorID
LEFT JOIN Department dept ON dept.ID = d.DepartmentID
ORDER BY a.AppointmentDate;

-- Prescriptions with items and inventory
SELECT rx.ID AS PrescriptionID, rx.PrescriptionDate,
       pat.FirstName AS PatientFirst, pat.LastName AS PatientLast,
       doc.FirstName AS DoctorFirst, doc.LastName AS DoctorLast,
       item.Dosage, item.DurationDays,
       inv.Name AS Medication, inv.Type, inv.Unit
FROM Prescription rx
JOIN Patient pat ON pat.ID = rx.PatientID
JOIN Doctor doc ON doc.ID = rx.DoctorID
JOIN PrescriptionItem item ON item.PrescriptionID = rx.ID
JOIN MedicalInventory inv ON inv.ID = item.MedicalInventoryID
ORDER BY rx.ID, inv.Name;

-- Feedback with patient + doctor
SELECT f.ID, f.Rating, f.Comments, f.FeedbackDate,
       p.FirstName AS PatientFirst, p.LastName AS PatientLast,
       d.FirstName AS DoctorFirst, d.LastName AS DoctorLast
FROM PatientFeedback f
JOIN Patient p ON p.ID = f.PatientID
JOIN Doctor d ON d.ID = f.DoctorID
ORDER BY f.FeedbackDate;