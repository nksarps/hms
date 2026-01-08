# Hospital Management System – ER Diagram Description

## Entities and Attributes

### 1. MedicalInventory
The **MedicalInventory** entity manages the hospital's medical supplies and equipment inventory.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each inventory item  
- **Name (VARCHAR(100))**: Name of the medical item or supply  
- **Type (VARCHAR(100))**: Category or classification of the medical item  
- **Quantity (INT)**: Current stock quantity available  
- **Unit (VARCHAR(20))**: Unit of measurement (e.g., boxes, bottles, pieces)  
- **ExpiryDate (DATE)**: Expiration date of the medical item  
- **Cost (DECIMAL(10, 2))**: Cost per unit of the item  

---

### 2. PrescriptionItem
The **PrescriptionItem** entity represents individual items or medications prescribed to patients.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each prescription item  
- **PrescriptionID (INT)** – Foreign Key: References the associated prescription  
- **MedicalInventoryID (INT)** – Foreign Key: References the medical inventory item  
- **Dosage (VARCHAR(255))**: Dosage instructions for the medication  
- **DurationDays (INT)**: Duration in days for which the medication should be taken  

---

### 3. Prescription
The **Prescription** entity stores prescription information issued by doctors to patients.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each prescription  
- **PatientID (INT)** – Foreign Key: References the patient receiving the prescription  
- **DoctorID (INT)** – Foreign Key: References the doctor issuing the prescription  
- **PrescriptionDate (DATE)**: Date when the prescription was issued  
- **Notes (TEXT)**: Additional notes or instructions for the prescription  

---

### 4. Patient
The **Patient** entity contains comprehensive information about patients registered in the hospital system.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each patient  
- **FirstName (VARCHAR(50))**: Patient's first name  
- **MiddleName (VARCHAR(50))**: Patient's middle name  
- **LastName (VARCHAR(50))**: Patient's last name  
- **Email (VARCHAR(100))**: Patient's email address  
- **PhoneNumber (VARCHAR(15))**: Patient's contact phone number  
- **DateOfBirth (DATE)**: Patient's date of birth  
- **Address (VARCHAR(255))**: Patient's residential address  
- **RegistrationDate (TIMESTAMP)**: Date and time when the patient was registered  

---

### 5. Appointment
The **Appointment** entity manages scheduling of appointments between patients and doctors.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each appointment  
- **PatientID (INT)** – Foreign Key: References the patient booking the appointment  
- **DoctorID (INT)** – Foreign Key: References the doctor for the appointment  
- **AppointmentDate (DATETIME)**: Scheduled date and time of the appointment  
- **Reason (VARCHAR(255))**: Reason or purpose for the appointment  

---

### 6. Doctor
The **Doctor** entity stores information about medical professionals working in the hospital.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each doctor  
- **FirstName (VARCHAR(50))**: Doctor's first name  
- **LastName (VARCHAR(50))**: Doctor's last name  
- **Email (VARCHAR(100))**: Doctor's email address  
- **PhoneNumber (VARCHAR(15))**: Doctor's contact phone number  
- **DepartmentID (INT)** – Foreign Key: References the department where the doctor works  

---

### 7. Department
The **Department** entity represents different medical departments within the hospital.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each department  
- **Name (VARCHAR(100))**: Name of the department  
- **PhoneNumber (VARCHAR(15))**: Department's contact phone number  

---

### 8. PatientFeedback
The **PatientFeedback** entity captures patient feedback and ratings for doctors and services.

**Attributes:**
- **ID (INT)** – Primary Key: Unique identifier for each feedback entry  
- **PatientID (INT)** – Foreign Key: References the patient providing feedback  
- **DoctorID (INT)** – Foreign Key: References the doctor being reviewed  
- **Rating (INT)**: Numerical rating given by the patient  
- **Comments (TEXT)**: Detailed comments or feedback from the patient  
- **FeedbackDate (TIMESTAMP)**: Date and time when the feedback was submitted  

---

## Relationships

### 1. MedicalInventory → PrescriptionItem
- **Relationship Type:** One-to-Many  
- **Description:** One medical inventory item can be included in multiple prescription items.  
- **Foreign Key:** `PrescriptionItem.MedicalInventoryID` → `MedicalInventory.ID`  

### 2. Prescription → PrescriptionItem
- **Relationship Type:** One-to-Many 
- **Description:** One prescription can include multiple prescription items.  
- **Foreign Key:** `PrescriptionItem.PrescriptionID` → `Prescription.ID`  

### 3. Patient → Prescription
- **Relationship Type:** One-to-Many  
- **Description:** One patient can have multiple prescriptions over time.  
- **Foreign Key:** `Prescription.PatientID` → `Patient.ID`  

### 4. Doctor → Prescription
- **Relationship Type:** One-to-Many 
- **Description:** One doctor can issue multiple prescriptions to various patients.  
- **Foreign Key:** `Prescription.DoctorID` → `Doctor.ID`  

### 5. Patient → Appointment
- **Relationship Type:** One-to-Many 
- **Description:** One patient can book multiple appointments over time.  
- **Foreign Key:** `Appointment.PatientID` → `Patient.ID`  

### 6. Doctor → Appointment
- **Relationship Type:** One-to-Many 
- **Description:** One doctor can have multiple appointments scheduled.  
- **Foreign Key:** `Appointment.DoctorID` → `Doctor.ID`  

### 7. Department → Doctor
- **Relationship Type:** One-to-Many 
- **Description:** One department employs multiple doctors.  
- **Foreign Key:** `Doctor.DepartmentID` → `Department.ID`  

### 8. Patient → PatientFeedback
- **Relationship Type:** One-to-Many 
- **Description:** One patient can provide multiple feedback entries.  
- **Foreign Key:** `PatientFeedback.PatientID` → `Patient.ID`  

### 9. Doctor → PatientFeedback
- **Relationship Type:** One-to-Many 
- **Description:** One doctor can receive multiple feedback entries.  
- **Foreign Key:** `PatientFeedback.DoctorID` → `Doctor.ID`  

---

## System Overview

This Hospital Management System ER diagram represents a comprehensive database design that manages the core operations of a healthcare facility. The system facilitates patient registration, appointment scheduling, prescription management, medical inventory tracking, and patient feedback collection. The relationships between entities ensure data integrity and enable efficient tracking of patient care, doctor activities, and resource management across different departments.
