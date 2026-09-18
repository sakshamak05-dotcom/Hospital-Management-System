package com.hospital.model;

import com.hospital.exception.HospitalException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Links a Patient to a Doctor on a given date and slot. Does not hold object
 * references to either, only their IDs — that keeps serialisation to a flat CSV
 * line simple and avoids duplicating patient data across files.
 */
public class Appointment {

    /** Lifecycle of an appointment. Using an enum instead of free-text status
     *  means an invalid state cannot be constructed at all. */
    public enum Status {
        SCHEDULED, COMPLETED, CANCELLED
    }

    private final String id;
    private final String patientId;
    private final String doctorId;
    private LocalDate date;
    private String slot;
    private Status status;

    public Appointment(String id, String patientId, String doctorId,
                       LocalDate date, String slot, Status status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.slot = slot;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toCsv() {
        return String.join(",", id, patientId, doctorId,
                date.toString(), slot, status.name());
    }

    public static Appointment fromCsv(String line) throws HospitalException {
        String[] p = line.split(",", -1);
        if (p.length != 6) {
            throw new HospitalException("Malformed appointment record: " + line);
        }
        try {
            return new Appointment(p[0], p[1], p[2], LocalDate.parse(p[3]),
                    p[4], Status.valueOf(p[5]));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new HospitalException("Bad date or status in record: " + line, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%-6s %-6s %-6s %-12s %-12s %s",
                id, patientId, doctorId, date, slot, status);
    }
}
