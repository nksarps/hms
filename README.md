# HMS

JavaFX desktop UI for hospital administration (patients and doctors) backed by MySQL via JDBC.

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- `.env` file for database connection credentials

## Database Setup

### 1. Create Database and Schema
```bash
mysql -u root -p < db/schema.sql
```
This creates the `hms` database and all required tables (Patient, Doctor, Department, Appointment, Prescription, PrescriptionItem, MedicalInventory, PatientFeedback).

### 2. Apply Search Indexes
```bash
mysql -u root -p hms < db/search_indexes.sql
```
Adds indexes on frequently searched columns to improve LIKE query performance.

### 3. Load Sample Data
```bash
mysql -u root -p hms < db/sample_data.sql
```
Populates the database with 43 sample records per table for testing and demos.

## Configuration

Create a `.env` file in the project root with your database credentials:
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=hms
DB_USER=your_username
DB_PASSWORD=your_password
```

## Running the Application

```bash
mvn javafx:run
```

## Documentation

- **ERD**: See [docs/hms-erd.jpeg](docs/hms-erd.jpeg) for the entity-relationship diagram
- **Database Schema**: See [docs/database.md](docs/database.md) for detailed table descriptions, fields, and relationships

## Testing Evidence

Database validation queries from `db/queries.sql` demonstrate correct schema implementation and data relationships.

### Table Population Verification
![Department Table](docs/screenshots/test-department-table.png)
*All 43 departments successfully created with unique names and phone numbers*

![Doctor Table](docs/screenshots/test-doctor-table.png)
*All 43 doctors loaded with proper foreign key references to departments*

### Relationship Validation

#### Appointments with Patient and Doctor Names
![Appointments Query](docs/screenshots/test-appointments-joined.png)
*Joined query showing appointments correctly linking patients and doctors with department information. All foreign keys intact.*

#### Prescriptions with Medication Details
![Prescriptions Query](docs/screenshots/test-prescriptions-inventory.png)
*Complex join across Prescription → PrescriptionItem → MedicalInventory tables. Demonstrates proper linking of prescriptions to specific medications with dosage and duration.*

#### Patient Feedback with Names
![Feedback Query](docs/screenshots/test-feedback-joined.png)
*Patient feedback entries correctly associated with both patient and doctor records, showing ratings (1-5) and comments.*

### Validation Summary
- ✅ All 8 tables populated with records
- ✅ Foreign key constraints enforced (Department → Doctor, Patient/Doctor → Appointment, etc.)
- ✅ Unique constraints working (emails, phone numbers)
- ✅ Complex multi-table joins executing successfully

