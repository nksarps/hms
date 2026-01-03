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

