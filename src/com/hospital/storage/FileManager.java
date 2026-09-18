package com.hospital.storage;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all disk I/O. Nothing else in the project touches the filesystem, so
 * swapping CSV files for a database later would mean rewriting this class only.
 *
 * Storage format is one CSV record per line, one file per entity type, under
 * the data/ directory.
 */
public class FileManager {

    private final Path dataDir;
    private final Path patientsFile;
    private final Path doctorsFile;
    private final Path appointmentsFile;
    private final Path billsFile;

    public FileManager(String dataDirectory) throws HospitalException {
        this.dataDir = Paths.get(dataDirectory);
        this.patientsFile = dataDir.resolve("patients.csv");
        this.doctorsFile = dataDir.resolve("doctors.csv");
        this.appointmentsFile = dataDir.resolve("appointments.csv");
        this.billsFile = dataDir.resolve("bills.csv");
        ensureDataDirectory();
    }

    /** Creates data/ on first run so a fresh clone works without manual setup. */
    private void ensureDataDirectory() throws HospitalException {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            throw new HospitalException("Could not create data directory: " + dataDir, e);
        }
    }

    private List<String> readLines(Path file) throws HospitalException {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines; // first run — nothing saved yet, not an error
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new HospitalException("Could not read " + file.getFileName(), e);
        }
        return lines;
    }

    private void writeLines(Path file, List<String> lines) throws HospitalException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new HospitalException("Could not write " + file.getFileName(), e);
        }
    }

    // ---------------- Patients ----------------

    public List<Patient> loadPatients() throws HospitalException {
        List<Patient> patients = new ArrayList<>();
        for (String line : readLines(patientsFile)) {
            patients.add(Patient.fromCsv(line));
        }
        return patients;
    }

    public void savePatients(List<Patient> patients) throws HospitalException {
        List<String> lines = new ArrayList<>();
        for (Patient p : patients) {
            lines.add(p.toCsv());
        }
        writeLines(patientsFile, lines);
    }

    // ---------------- Doctors ----------------

    public List<Doctor> loadDoctors() throws HospitalException {
        List<Doctor> doctors = new ArrayList<>();
        for (String line : readLines(doctorsFile)) {
            doctors.add(Doctor.fromCsv(line));
        }
        return doctors;
    }

    public void saveDoctors(List<Doctor> doctors) throws HospitalException {
        List<String> lines = new ArrayList<>();
        for (Doctor d : doctors) {
            lines.add(d.toCsv());
        }
        writeLines(doctorsFile, lines);
    }

    // ---------------- Appointments ----------------

    public List<Appointment> loadAppointments() throws HospitalException {
        List<Appointment> appointments = new ArrayList<>();
        for (String line : readLines(appointmentsFile)) {
            appointments.add(Appointment.fromCsv(line));
        }
        return appointments;
    }

    public void saveAppointments(List<Appointment> appointments) throws HospitalException {
        List<String> lines = new ArrayList<>();
        for (Appointment a : appointments) {
            lines.add(a.toCsv());
        }
        writeLines(appointmentsFile, lines);
    }

    // ---------------- Bills ----------------

    public List<Bill> loadBills() throws HospitalException {
        List<Bill> bills = new ArrayList<>();
        for (String line : readLines(billsFile)) {
            bills.add(Bill.fromCsv(line));
        }
        return bills;
    }

    public void saveBills(List<Bill> bills) throws HospitalException {
        List<String> lines = new ArrayList<>();
        for (Bill b : bills) {
            lines.add(b.toCsv());
        }
        writeLines(billsFile, lines);
    }
}
