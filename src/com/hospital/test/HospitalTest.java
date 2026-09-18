package com.hospital.test;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.Hospital;
import com.hospital.storage.FileManager;
import com.hospital.util.InputValidator;

import java.time.LocalDate;

/**
 * Validation tests written without JUnit, so the project compiles and runs with
 * nothing but a JDK — no jar downloads, no build tool. Each check prints PASS or
 * FAIL and the process exits non-zero if anything failed.
 *
 * Writes to a throwaway directory (data-test) so a test run never touches the
 * real data files.
 */
public class HospitalTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Running validation tests...\n");

        FileManager fm = new FileManager("data-test");
        Hospital hospital = new Hospital(fm);
        hospital.loadAll();

        testValidation();
        testPatientCrud(hospital);
        testDoctorCrud(hospital);
        testAppointmentRules(hospital);
        testBillingRules(hospital);
        testSearch(hospital);
        testPersistence(fm);

        System.out.printf("%n----------------------------%n");
        System.out.printf(" Passed: %d   Failed: %d%n", passed, failed);
        System.out.printf("----------------------------%n");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------- Test cases ----------------

    private static void testValidation() {
        System.out.println("[Input validation]");
        rejects("age 200 rejected", () -> InputValidator.requireAge("200"));
        rejects("age 'abc' rejected", () -> InputValidator.requireAge("abc"));
        rejects("9-digit phone rejected", () -> InputValidator.requirePhone("98765432"));
        rejects("empty name rejected", () -> InputValidator.requireText("  ", "Name"));
        rejects("name with comma rejected", () -> InputValidator.requireText("Rao, S", "Name"));
        rejects("gender 'X' rejected", () -> InputValidator.requireGender("X"));
        rejects("blood group 'C+' rejected", () -> InputValidator.requireBloodGroup("C+"));
        rejects("negative amount rejected", () -> InputValidator.requireAmount("-50", "Fee"));
        rejects("past date rejected", () -> InputValidator.requireFutureDate("2020-01-01"));
        accepts("valid 10-digit phone accepted", () -> InputValidator.requirePhone("9876543210"));
        accepts("blood group 'AB-' accepted", () -> InputValidator.requireBloodGroup("ab-"));
    }

    private static void testPatientCrud(Hospital h) throws HospitalException {
        System.out.println("\n[Patient module]");
        int before = h.listPatients().size();
        Patient p = h.addPatient("Test Patient", 30, "M", "9000000001",
                "Fever", "O+", false);
        check("patient gets an ID", p.getId().startsWith("P"));
        check("patient count increased", h.listPatients().size() == before + 1);
        check("patient retrievable by ID", h.getPatient(p.getId()).getName().equals("Test Patient"));

        h.updatePatient(p.getId(), "Renamed Patient", null, null, true);
        check("name updated", h.getPatient(p.getId()).getName().equals("Renamed Patient"));
        check("admitted flag updated", h.getPatient(p.getId()).isAdmitted());
        check("blank fields left unchanged", h.getPatient(p.getId()).getAilment().equals("Fever"));

        rejects("unknown patient ID throws", () -> h.getPatient("P999"));
    }

    private static void testDoctorCrud(Hospital h) throws HospitalException {
        System.out.println("\n[Doctor module]");
        Doctor d = h.addDoctor("Test Doctor", 45, "F", "9000000002",
                "Cardiology", 800.0, true);
        check("doctor gets an ID", d.getId().startsWith("D"));
        check("fee stored correctly", h.getDoctor(d.getId()).getConsultationFee() == 800.0);

        h.updateDoctor(d.getId(), null, null, 950.0, null);
        check("fee updated", h.getDoctor(d.getId()).getConsultationFee() == 950.0);
        rejects("unknown doctor ID throws", () -> h.getDoctor("D999"));
    }

    private static void testAppointmentRules(Hospital h) throws HospitalException {
        System.out.println("\n[Appointment module]");
        Patient p = h.addPatient("Appt Patient", 28, "F", "9000000003", "Checkup", "A+", false);
        Doctor d = h.addDoctor("Appt Doctor", 50, "M", "9000000004", "General", 500.0, true);
        Doctor onLeave = h.addDoctor("Leave Doctor", 40, "M", "9000000005", "Ortho", 600.0, false);
        LocalDate date = LocalDate.now().plusDays(3);

        Appointment a = h.bookAppointment(p.getId(), d.getId(), date, "10:00-10:30");
        check("appointment booked", a.getStatus() == Appointment.Status.SCHEDULED);

        rejects("double-booking same slot rejected",
                () -> h.bookAppointment(p.getId(), d.getId(), date, "10:00-10:30"));
        rejects("unavailable doctor rejected",
                () -> h.bookAppointment(p.getId(), onLeave.getId(), date, "11:00-11:30"));
        rejects("deleting patient with live appointment rejected",
                () -> h.deletePatient(p.getId()));

        h.completeAppointment(a.getId());
        check("appointment completed", h.getAppointment(a.getId()).getStatus()
                == Appointment.Status.COMPLETED);
        rejects("completing twice rejected", () -> h.completeAppointment(a.getId()));
    }

    private static void testBillingRules(Hospital h) throws HospitalException {
        System.out.println("\n[Billing module]");
        Patient p = h.addPatient("Bill Patient", 60, "M", "9000000006", "Surgery", "B+", true);
        Doctor d = h.addDoctor("Bill Doctor", 55, "F", "9000000007", "Surgery", 1000.0, true);
        Appointment a = h.bookAppointment(p.getId(), d.getId(),
                LocalDate.now().plusDays(1), "14:00-14:30");

        rejects("billing a SCHEDULED appointment rejected",
                () -> h.generateBill(a.getId(), 2000, 500));

        h.completeAppointment(a.getId());
        Bill bill = h.generateBill(a.getId(), 2000, 500);

        check("subtotal = 1000 + 2000 + 500", bill.getSubtotal() == 3500.0);
        check("tax is 5% of subtotal", Math.abs(bill.getTax() - 175.0) < 0.001);
        check("total = subtotal + tax", Math.abs(bill.getTotal() - 3675.0) < 0.001);
        check("new bill is unpaid", !bill.isPaid());

        rejects("double-billing rejected", () -> h.generateBill(a.getId(), 100, 100));

        double outstandingBefore = h.totalOutstanding();
        h.markBillPaid(bill.getId());
        check("bill marked paid", h.getBill(bill.getId()).isPaid());
        check("outstanding dropped by bill total",
                Math.abs(outstandingBefore - h.totalOutstanding() - 3675.0) < 0.001);
        rejects("paying twice rejected", () -> h.markBillPaid(bill.getId()));
    }

    private static void testSearch(Hospital h) throws HospitalException {
        System.out.println("\n[Search module]");
        h.addPatient("Searchable Person", 33, "F", "9000000008", "Migraine", "AB+", false);
        check("search finds by name", !h.searchPatients("searchable").isEmpty());
        check("search finds by ailment", !h.searchPatients("migraine").isEmpty());
        check("search is case-insensitive", !h.searchPatients("SEARCHABLE").isEmpty());
        check("no match returns empty list", h.searchPatients("zzzznotthere").isEmpty());
        check("doctor search by specialization", !h.searchDoctors("cardio").isEmpty());
    }

    private static void testPersistence(FileManager fm) throws HospitalException {
        System.out.println("\n[File storage]");
        Hospital reloaded = new Hospital(fm);
        reloaded.loadAll();
        check("patients survive a reload", !reloaded.listPatients().isEmpty());
        check("doctors survive a reload", !reloaded.listDoctors().isEmpty());
        check("appointments survive a reload", !reloaded.listAppointments().isEmpty());
        check("bills survive a reload", !reloaded.listBills().isEmpty());

        rejects("malformed CSV line rejected", () -> Patient.fromCsv("P001,Incomplete,30"));
    }

    // ---------------- Assertion helpers ----------------

    /** Any operation that may throw a HospitalException. */
    private interface Action {
        void run() throws HospitalException;
    }

    private static void check(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + label);
        } else {
            failed++;
            System.out.println("  FAIL  " + label);
        }
    }

    /** Passes when the action DOES throw — used for invalid-input cases. */
    private static void rejects(String label, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("  FAIL  " + label + " (no exception was thrown)");
        } catch (HospitalException e) {
            passed++;
            System.out.println("  PASS  " + label);
        }
    }

    /** Passes when the action does NOT throw. */
    private static void accepts(String label, Action action) {
        try {
            action.run();
            passed++;
            System.out.println("  PASS  " + label);
        } catch (HospitalException e) {
            failed++;
            System.out.println("  FAIL  " + label + " (" + e.getMessage() + ")");
        }
    }
}
