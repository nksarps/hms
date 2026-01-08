package com.nks.hms.model;

import java.time.LocalDate;

/**
 * Domain model representing a patient in the hospital management system.
 * Contains basic demographic and contact information needed for patient registration
 * and management. Maps directly to the 'patient' table in the MySQL database.
 * 
 * @see com.nks.hms.repository.PatientRepository
 */
public class Patient {
    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;

    /**
     * Default no-arg constructor required by JavaFX for data binding
     * and by JDBC when creating instances from result sets.
     */
    public Patient() {
    }

    /**
     * Full constructor for creating a patient with all fields initialized.
     * Typically used when mapping database results to Patient objects.
     * 
     * @param id Database primary key, null for new patients
     * @param firstName Patient's first name (required)
     * @param middleName Patient's middle name (optional)
     * @param lastName Patient's last name (required)
     * @param dateOfBirth Patient's date of birth (required)
     * @param phone Patient's phone number (required)
     * @param email Patient's email address (required)
     * @param address Patient's physical address (optional)
     */
    public Patient(Integer id, String firstName, String middleName, String lastName, LocalDate dateOfBirth, String phone, String email, String address) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns a human-readable representation of the patient.
     * Used for display purposes in UI components like combo boxes and tables.
     * 
     * @return Full name in "FirstName LastName" format
     */
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
