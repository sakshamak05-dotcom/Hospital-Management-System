package com.hospital.service;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.storage.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The service layer: every rule about what the hospital can and cannot do lives
 * here. Main only collects input and prints output; it contains no logic of its
 * own. That separation is what makes HospitalTest possible without simulating
 * keyboard input.
 *
 * Records are held in LinkedHashMaps keyed by ID. A map gives O(1) lookup by ID
 * (the most common operation), and LinkedHashMap preserves insertion order so
 * listings appear in the order records were created rather than scrambled.
 */
public class Hospital {

    private final Map<String, Patient> patients = new LinkedHashMap<>();
    private final Map<String, Doctor> doctors = new LinkedHashMap<>();
    private final Map<String, Appointment> appointments = new LinkedHashMap<>();
    private final Map<String, Bill> bills = new LinkedHashMap<>();

    private final FileManager fileManager;

    public Hospital(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    // ---------------- Persistence ----------------

    /** Loads everything from disk into memory. Called once at startup. */
    public void loadAll() throws HospitalException {
        patients.clear();
        doctors.clear();
        appointments.clear();
        bills.clear();

        for (Patient p : fileManager.loadPatients()) {
            patients.put(p.getId(), p);
        }
        for (Doctor d : fileManager.loadDoctors()) {
            doctors.put(d.getId(), d);
        }
        for (Appointment a : fileManager.loadAppointments()) {
            appointments.put(a.getId(), a);
        }
        for (Bill b : fileManager.loadBills()) {
            bills.put(b.getId(), b);
        }
    }

    /**
     * Writes everything back to disk. Called after each mutating operation so an
     * unexpected exit never loses more than the operation in progress. For this
     * data volume a full rewrite is cheap and far simpler than tracking deltas.
     */
    public void saveAll() throws HospitalException {
        fileManager.savePatients(new ArrayList<>(patients.values()));
        fileManager.saveDoctors(new ArrayList<>(doctors.values()));
        fileManager.saveAppointments(new ArrayList<>(appointments.values()));
        fileManager.saveBills(new ArrayList<>(bills.values()));
    }

    // ---------------- ID generation ----------------

    /**
     * Produces the next ID for a prefix, e.g. P001 -> P002. Scans existing keys
     * for the highest number rather than using a counter, so IDs stay correct
     * across restarts without storing the counter anywhere.
     */
    private String nextId(String prefix, Map<String, ?> existing) {
        int max = 0;
        for (String id : existing.keySet()) {
            try {
                int n = Integer.parseInt(id.substring(prefix.length()));
                if (n > max) {
                    max = n;
                }
            } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                // a hand-edited ID that does not fit the pattern; skip it
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    // ---------------- Patient management ----------------

    public Patient addPatient(String name, int age, String gender, String phone,
                              String ailment, String bloodGroup, boolean admitted)
            throws HospitalException {
        String id = nextId("P", patients);
        Patient patient = new Patient(id, name, age, gender, phone, ailment, bloodGroup, admitted);
        patients.put(id, patient);
        saveAll();
        return patient;
    }

    public Patient getPatient(String id) throws HospitalException {
        Patient p = patients.get(id.toUpperCase());
        if (p == null) {
            throw new HospitalException("No patient found with ID " + id);
        }
        return p;
    }

    public List<Patient> listPatients() {
        return new ArrayList<>(patients.values());
    }

    public void updatePatient(String id, String name, String phone,
                              String ailment, Boolean admitted) throws HospitalException {
        Patient p = getPatient(id);
        if (name != null) {
            p.setName(name);
        }
        if (phone != null) {
            p.setPhone(phone);
        }
        if (ailment != null) {
            p.setAilment(ailment);
        }
        if (admitted != null) {
            p.setAdmitted(admitted);
        }
        saveAll();
    }

    /**
     * Deletes a patient, but refuses if appointments still reference them.
     * Without this check the appointment file would hold a dangling patient ID.
     */
    public void deletePatient(String id) throws HospitalException {
        Patient p = getPatient(id);
        for (Appointment a : appointments.values()) {
            if (a.getPatientId().equals(p.getId())
                    && a.getStatus() == Appointment.Status.SCHEDULED) {
                throw new HospitalException(
                        "Cannot delete " + p.getId() + ": appointment " + a.getId()
                        + " is still scheduled. Cancel it first.");
            }
        }
        patients.remove(p.getId());
        saveAll();
    }

    // ---------------- Doctor management ----------------

    public Doctor addDoctor(String name, int age, String gender, String phone,
                            String specialization, double fee, boolean available)
            throws HospitalException {
        String id = nextId("D", doctors);
        Doctor doctor = new Doctor(id, name, age, gender, phone, specialization, fee, available);
        doctors.put(id, doctor);
        saveAll();
        return doctor;
    }

    public Doctor getDoctor(String id) throws HospitalException {
        Doctor d = doctors.get(id.toUpperCase());
        if (d == null) {
            throw new HospitalException("No doctor found with ID " + id);
        }
        return d;
    }

    public List<Doctor> listDoctors() {
        return new ArrayList<>(doctors.values());
    }

    public void updateDoctor(String id, String name, String phone,
                             Double fee, Boolean available) throws HospitalException {
        Doctor d = getDoctor(id);
        if (name != null) {
            d.setName(name);
        }
        if (phone != null) {
            d.setPhone(phone);
        }
        if (fee != null) {
            d.setConsultationFee(fee);
        }
        if (available != null) {
            d.setAvailable(available);
        }
        saveAll();
    }

    public void deleteDoctor(String id) throws HospitalException {
        Doctor d = getDoctor(id);
        for (Appointment a : appointments.values()) {
            if (a.getDoctorId().equals(d.getId())
                    && a.getStatus() == Appointment.Status.SCHEDULED) {
                throw new HospitalException(
                        "Cannot delete " + d.getId() + ": appointment " + a.getId()
                        + " is still scheduled. Cancel or reassign it first.");
            }
        }
        doctors.remove(d.getId());
        saveAll();
    }

    // ---------------- Appointment management ----------------

    /**
     * Books an appointment after three checks: both parties exist, the doctor is
     * on duty, and the slot is free. The double-booking check is the reason this
     * lives in the service layer rather than in the Appointment constructor — it
     * needs to see every other appointment.
     */
    public Appointment bookAppointment(String patientId, String doctorId,
                                       LocalDate date, String slot) throws HospitalException {
        Patient patient = getPatient(patientId);
        Doctor doctor = getDoctor(doctorId);

        if (!doctor.isAvailable()) {
            throw new HospitalException("Dr. " + doctor.getName() + " is currently on leave.");
        }

        for (Appointment a : appointments.values()) {
            if (a.getStatus() == Appointment.Status.SCHEDULED
                    && a.getDoctorId().equals(doctor.getId())
                    && a.getDate().equals(date)
                    && a.getSlot().equalsIgnoreCase(slot)) {
                throw new HospitalException("Dr. " + doctor.getName()
                        + " already has an appointment at " + slot + " on " + date + ".");
            }
        }

        String id = nextId("A", appointments);
        Appointment appointment = new Appointment(id, patient.getId(), doctor.getId(),
                date, slot, Appointment.Status.SCHEDULED);
        appointments.put(id, appointment);
        saveAll();
        return appointment;
    }

    public Appointment getAppointment(String id) throws HospitalException {
        Appointment a = appointments.get(id.toUpperCase());
        if (a == null) {
            throw new HospitalException("No appointment found with ID " + id);
        }
        return a;
    }

    public List<Appointment> listAppointments() {
        return new ArrayList<>(appointments.values());
    }

    public void cancelAppointment(String id) throws HospitalException {
        Appointment a = getAppointment(id);
        if (a.getStatus() == Appointment.Status.COMPLETED) {
            throw new HospitalException("Appointment " + id + " is already completed.");
        }
        a.setStatus(Appointment.Status.CANCELLED);
        saveAll();
    }

    public void completeAppointment(String id) throws HospitalException {
        Appointment a = getAppointment(id);
        if (a.getStatus() != Appointment.Status.SCHEDULED) {
            throw new HospitalException("Only a scheduled appointment can be completed.");
        }
        a.setStatus(Appointment.Status.COMPLETED);
        saveAll();
    }

    // ---------------- Billing ----------------

    /**
     * Generates a bill for a completed appointment. The consultation fee is
     * copied from the doctor at billing time rather than read live afterwards,
     * so a later fee change does not silently alter old invoices.
     */
    public Bill generateBill(String appointmentId, double roomCharges,
                             double medicineCharges) throws HospitalException {
        Appointment appointment = getAppointment(appointmentId);

        if (appointment.getStatus() != Appointment.Status.COMPLETED) {
            throw new HospitalException(
                    "Bill can only be generated for a COMPLETED appointment. "
                    + appointmentId + " is " + appointment.getStatus() + ".");
        }
        for (Bill existing : bills.values()) {
            if (existing.getAppointmentId().equals(appointment.getId())) {
                throw new HospitalException("Appointment " + appointmentId
                        + " has already been billed as " + existing.getId() + ".");
            }
        }

        Doctor doctor = getDoctor(appointment.getDoctorId());
        String id = nextId("B", bills);
        Bill bill = new Bill(id, appointment.getPatientId(), appointment.getId(),
                doctor.getConsultationFee(), roomCharges, medicineCharges, false);
        bills.put(id, bill);
        saveAll();
        return bill;
    }

    public Bill getBill(String id) throws HospitalException {
        Bill b = bills.get(id.toUpperCase());
        if (b == null) {
            throw new HospitalException("No bill found with ID " + id);
        }
        return b;
    }

    public List<Bill> listBills() {
        return new ArrayList<>(bills.values());
    }

    public void markBillPaid(String id) throws HospitalException {
        Bill b = getBill(id);
        if (b.isPaid()) {
            throw new HospitalException("Bill " + id + " is already marked paid.");
        }
        b.setPaid(true);
        saveAll();
    }

    public double totalOutstanding() {
        double sum = 0;
        for (Bill b : bills.values()) {
            if (!b.isPaid()) {
                sum += b.getTotal();
            }
        }
        return sum;
    }

    // ---------------- Search ----------------

    /** Case-insensitive substring match on name, ailment or blood group. */
    public List<Patient> searchPatients(String keyword) {
        String k = keyword.toLowerCase();
        List<Patient> results = new ArrayList<>();
        for (Patient p : patients.values()) {
            if (p.getName().toLowerCase().contains(k)
                    || p.getAilment().toLowerCase().contains(k)
                    || p.getBloodGroup().toLowerCase().contains(k)) {
                results.add(p);
            }
        }
        return results;
    }

    public List<Doctor> searchDoctors(String keyword) {
        String k = keyword.toLowerCase();
        List<Doctor> results = new ArrayList<>();
        for (Doctor d : doctors.values()) {
            if (d.getName().toLowerCase().contains(k)
                    || d.getSpecialization().toLowerCase().contains(k)) {
                results.add(d);
            }
        }
        return results;
    }

    public List<Appointment> appointmentsForPatient(String patientId) throws HospitalException {
        Patient p = getPatient(patientId);
        List<Appointment> results = new ArrayList<>();
        for (Appointment a : appointments.values()) {
            if (a.getPatientId().equals(p.getId())) {
                results.add(a);
            }
        }
        return results;
    }

    public List<Appointment> appointmentsOnDate(LocalDate date) {
        List<Appointment> results = new ArrayList<>();
        for (Appointment a : appointments.values()) {
            if (a.getDate().equals(date)) {
                results.add(a);
            }
        }
        return results;
    }

    // ---------------- Reporting ----------------

    public int countAdmitted() {
        int count = 0;
        for (Patient p : patients.values()) {
            if (p.isAdmitted()) {
                count++;
            }
        }
        return count;
    }

    public int countByStatus(Appointment.Status status) {
        int count = 0;
        for (Appointment a : appointments.values()) {
            if (a.getStatus() == status) {
                count++;
            }
        }
        return count;
    }
}
