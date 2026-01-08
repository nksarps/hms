-- Purpose: Database and table creation for HMS

CREATE DATABASE hms;

USE hms;

CREATE TABLE Department (
    ID           INT AUTO_INCREMENT PRIMARY KEY,
    Name         VARCHAR(100) NOT NULL,
    PhoneNumber  VARCHAR(15),
    CONSTRAINT uq_department_name UNIQUE (Name)
);

CREATE TABLE Doctor (
    ID            INT AUTO_INCREMENT PRIMARY KEY,
    FirstName     VARCHAR(50) NOT NULL,
    MiddleName    VARCHAR(50),
    LastName      VARCHAR(50) NOT NULL,
    Email         VARCHAR(100) NOT NULL,
    PhoneNumber   VARCHAR(15),
    DepartmentID  INT,
    CONSTRAINT uq_doctor_email UNIQUE (Email),
    CONSTRAINT uq_doctor_phone UNIQUE (PhoneNumber),
    CONSTRAINT fk_doctor_department FOREIGN KEY (DepartmentID) REFERENCES Department(ID)
);

CREATE TABLE Patient (
    ID                INT AUTO_INCREMENT PRIMARY KEY,
    FirstName         VARCHAR(50) NOT NULL,
    MiddleName        VARCHAR(50),
    LastName          VARCHAR(50) NOT NULL,
    Email             VARCHAR(100) NOT NULL,
    PhoneNumber       VARCHAR(15),
    DateOfBirth       DATE,
    Address           VARCHAR(255),
    RegistrationDate  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_patient_email UNIQUE (Email),
    CONSTRAINT uq_patient_phone UNIQUE (PhoneNumber)
);

CREATE TABLE Appointment (
    ID               INT AUTO_INCREMENT PRIMARY KEY,
    PatientID        INT NOT NULL,
    DoctorID         INT NOT NULL,
    AppointmentDate  DATETIME NOT NULL,
    Reason           VARCHAR(255),
    CONSTRAINT uq_appt_patient_doctor_datetime UNIQUE (PatientID, DoctorID, AppointmentDate),
    CONSTRAINT fk_appt_patient FOREIGN KEY (PatientID) REFERENCES Patient(ID),
    CONSTRAINT fk_appt_doctor FOREIGN KEY (DoctorID) REFERENCES Doctor(ID)
);

CREATE TABLE Prescription (
    ID                INT AUTO_INCREMENT PRIMARY KEY,
    PatientID         INT NOT NULL,
    DoctorID          INT NOT NULL,
    PrescriptionDate  DATE NOT NULL,
    Notes             TEXT,
    CONSTRAINT fk_rx_patient FOREIGN KEY (PatientID) REFERENCES Patient(ID),
    CONSTRAINT fk_rx_doctor FOREIGN KEY (DoctorID) REFERENCES Doctor(ID)
);

CREATE TABLE MedicalInventory (
    ID          INT AUTO_INCREMENT PRIMARY KEY,
    Name        VARCHAR(100) NOT NULL,
    Type        VARCHAR(100),
    Quantity    INT,
    Unit        VARCHAR(20),
    ExpiryDate  DATE,
    Cost        DECIMAL(10,2),
    CONSTRAINT uq_inventory_name UNIQUE (Name)
);

CREATE TABLE PrescriptionItem (
    ID                  INT AUTO_INCREMENT PRIMARY KEY,
    PrescriptionID      INT NOT NULL,
    MedicalInventoryID  INT NOT NULL,
    Dosage              VARCHAR(255),
    DurationDays        INT,
    CONSTRAINT fk_item_rx FOREIGN KEY (PrescriptionID) REFERENCES Prescription(ID),
    CONSTRAINT fk_item_inventory FOREIGN KEY (MedicalInventoryID) REFERENCES MedicalInventory(ID)
);

CREATE TABLE PatientFeedback (
    ID            INT AUTO_INCREMENT PRIMARY KEY,
    PatientID     INT NOT NULL,
    DoctorID      INT NOT NULL,
    Rating        INT,
    Comments      TEXT,
    FeedbackDate  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_feedback_patient_doctor_date UNIQUE (PatientID, DoctorID, FeedbackDate),
    CONSTRAINT fk_feedback_patient FOREIGN KEY (PatientID) REFERENCES Patient(ID),
    CONSTRAINT fk_feedback_doctor FOREIGN KEY (DoctorID) REFERENCES Doctor(ID)
);

