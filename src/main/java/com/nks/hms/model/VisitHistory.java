package com.nks.hms.model;

import java.time.LocalDate;

public class VisitHistory {
    private final String doctorName;
    private final LocalDate visitDate;
    private final String reason;
    private final String notes;

    public VisitHistory(String doctorName, LocalDate visitDate, String reason, String notes) {
        this.doctorName = doctorName;
        this.visitDate = visitDate;
        this.reason = reason;
        this.notes = notes;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public String getReason() {
        return reason;
    }

    public String getNotes() {
        return notes;
    }
}
