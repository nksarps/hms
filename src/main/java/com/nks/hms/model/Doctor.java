package com.nks.hms.model;

/**
 * Domain model representing a doctor in the hospital management system.
 * Contains basic contact information and department association.
 * Maps directly to the 'doctor' table in the MySQL database.
 * 
 * Note: Department association is optional (departmentId can be null),
 * allowing doctors to be registered before being assigned to a department.
 * 
 * @see com.nks.hms.repository.DoctorRepository
 */
public class Doctor {
    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;
    private Integer departmentId;

    /**
     * Default no-arg constructor required by JavaFX for data binding
     * and by JDBC when creating instances from result sets.
     */
    public Doctor() {
    }

    /**
     * Constructor for creating a doctor with basic information.
     * Department ID is not included and must be set separately if needed.
     * 
     * @param id Database primary key, null for new doctors
     * @param firstName Doctor's first name (required)
     * @param middleName Doctor's middle name (optional)
     * @param lastName Doctor's last name (required)
     * @param phone Doctor's phone number (required)
     * @param email Doctor's email address (required)
     */
    public Doctor(Integer id, String firstName, String middleName, String lastName, String phone, String email) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
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

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    /**
     * Returns a human-readable representation of the doctor.
     * Used for display purposes in UI components and appointment listings.
     * 
     * @return Full name in "FirstName LastName" format
     */
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
