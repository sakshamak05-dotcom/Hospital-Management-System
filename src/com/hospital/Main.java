package com.hospital;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.Hospital;
import com.hospital.storage.FileManager;
import com.hospital.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line front end. Its only jobs are to draw menus, read input, hand
 * values to Hospital, and print results. No business rule is decided here.
 *
 * Every menu action is wrapped in a try/catch for HospitalException so that a
 * bad input prints a message and returns to the menu instead of ending the
 * program.
 */
public class Main {

    private static final String DATA_DIR = "data";

    private final Scanner scanner = new Scanner(System.in);
    private final Hospital hospital;

    public Main(Hospital hospital) {
        this.hospital = hospital;
    }

    public static void main(String[] args) {
        try {
            FileManager fileManager = new FileManager(DATA_DIR);
            Hospital hospital = new Hospital(fileManager);
            hospital.loadAll();
            new Main(hospital).run();
        } catch (HospitalException e) {
            System.err.println("Startup failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void run() {
        System.out.println("=========================================");
        System.out.println("   HOSPITAL MANAGEMENT SYSTEM");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Choose an option");
            switch (choice) {
                case "1" -> patientMenu();
                case "2" -> doctorMenu();
                case "3" -> appointmentMenu();
                case "4" -> billingMenu();
                case "5" -> searchMenu();
                case "6" -> showSummary();
                case "0" -> {
                    System.out.println("Saving and exiting. Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid option. Enter a number from the menu.");
            }
        }
    }

    // ---------------- Menus ----------------

    private void printMainMenu() {
        System.out.println();
        System.out.println("--------- MAIN MENU ---------");
        System.out.println(" 1. Patient management");
        System.out.println(" 2. Doctor management");
        System.out.println(" 3. Appointment management");
        System.out.println(" 4. Billing");
        System.out.println(" 5. Search");
        System.out.println(" 6. Summary report");
        System.out.println(" 0. Exit");
    }

    private void patientMenu() {
        System.out.println();
        System.out.println("--- PATIENTS ---");
        System.out.println(" 1. Register patient");
        System.out.println(" 2. List all patients");
        System.out.println(" 3. View patient by ID");
        System.out.println(" 4. Update patient");
        System.out.println(" 5. Delete patient");
        System.out.println(" 0. Back");

        String choice = prompt("Choose an option");
        try {
            switch (choice) {
                case "1" -> registerPatient();
                case "2" -> printPatients(hospital.listPatients());
                case "3" -> System.out.println(hospital.getPatient(prompt("Patient ID")));
                case "4" -> updatePatient();
                case "5" -> {
                    hospital.deletePatient(prompt("Patient ID to delete"));
                    System.out.println("Patient deleted.");
                }
                case "0" -> { /* back to main menu */ }
                default -> System.out.println("Invalid option.");
            }
        } catch (HospitalException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void doctorMenu() {
        System.out.println();
        System.out.println("--- DOCTORS ---");
        System.out.println(" 1. Add doctor");
        System.out.println(" 2. List all doctors");
        System.out.println(" 3. View doctor by ID");
        System.out.println(" 4. Update doctor");
        System.out.println(" 5. Delete doctor");
        System.out.println(" 0. Back");

        String choice = prompt("Choose an option");
        try {
            switch (choice) {
                case "1" -> addDoctor();
                case "2" -> printDoctors(hospital.listDoctors());
                case "3" -> System.out.println(hospital.getDoctor(prompt("Doctor ID")));
                case "4" -> updateDoctor();
                case "5" -> {
                    hospital.deleteDoctor(prompt("Doctor ID to delete"));
                    System.out.println("Doctor deleted.");
                }
                case "0" -> { }
                default -> System.out.println("Invalid option.");
            }
        } catch (HospitalException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void appointmentMenu() {
        System.out.println();
        System.out.println("--- APPOINTMENTS ---");
        System.out.println(" 1. Book appointment");
        System.out.println(" 2. List all appointments");
        System.out.println(" 3. Mark appointment completed");
        System.out.println(" 4. Cancel appointment");
        System.out.println(" 5. Appointments on a date");
        System.out.println(" 0. Back");

        String choice = prompt("Choose an option");
        try {
            switch (choice) {
                case "1" -> bookAppointment();
                case "2" -> printAppointments(hospital.listAppointments());
                case "3" -> {
                    hospital.completeAppointment(prompt("Appointment ID"));
                    System.out.println("Appointment marked COMPLETED. A bill can now be generated.");
                }
                case "4" -> {
                    hospital.cancelAppointment(prompt("Appointment ID"));
                    System.out.println("Appointment CANCELLED.");
                }
                case "5" -> printAppointments(hospital.appointmentsOnDate(
                        LocalDate.parse(prompt("Date (yyyy-MM-dd)").trim())));
                case "0" -> { }
                default -> System.out.println("Invalid option.");
            }
        } catch (HospitalException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error: could not read that date. Use yyyy-MM-dd.");
        }
    }

    private void billingMenu() {
        System.out.println();
        System.out.println("--- BILLING ---");
        System.out.println(" 1. Generate bill");
        System.out.println(" 2. List all bills");
        System.out.println(" 3. View invoice");
        System.out.println(" 4. Mark bill as paid");
        System.out.println(" 5. Total outstanding");
        System.out.println(" 0. Back");

        String choice = prompt("Choose an option");
        try {
            switch (choice) {
                case "1" -> generateBill();
                case "2" -> printBills(hospital.listBills());
                case "3" -> System.out.print(hospital.getBill(prompt("Bill ID")).toReceipt());
                case "4" -> {
                    hospital.markBillPaid(prompt("Bill ID"));
                    System.out.println("Bill marked PAID.");
                }
                case "5" -> System.out.printf("Total outstanding: %.2f%n",
                        hospital.totalOutstanding());
                case "0" -> { }
                default -> System.out.println("Invalid option.");
            }
        } catch (HospitalException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void searchMenu() {
        System.out.println();
        System.out.println("--- SEARCH ---");
        System.out.println(" 1. Search patients (name / ailment / blood group)");
        System.out.println(" 2. Search doctors (name / specialization)");
        System.out.println(" 3. Appointment history for a patient");
        System.out.println(" 0. Back");

        String choice = prompt("Choose an option");
        try {
            switch (choice) {
                case "1" -> printPatients(hospital.searchPatients(prompt("Keyword")));
                case "2" -> printDoctors(hospital.searchDoctors(prompt("Keyword")));
                case "3" -> printAppointments(
                        hospital.appointmentsForPatient(prompt("Patient ID")));
                case "0" -> { }
                default -> System.out.println("Invalid option.");
            }
        } catch (HospitalException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- Actions ----------------

    private void registerPatient() throws HospitalException {
        String name = InputValidator.requireText(prompt("Name"), "Name");
        int age = InputValidator.requireAge(prompt("Age"));
        String gender = InputValidator.requireGender(prompt("Gender (M/F/O)"));
        String phone = InputValidator.requirePhone(prompt("Phone (10 digits)"));
        String ailment = InputValidator.requireText(prompt("Ailment"), "Ailment");
        String bloodGroup = InputValidator.requireBloodGroup(prompt("Blood group"));
        boolean admitted = InputValidator.requireYesNo(prompt("Admitted? (y/n)"));

        Patient p = hospital.addPatient(name, age, gender, phone, ailment, bloodGroup, admitted);
        System.out.println("Registered with ID " + p.getId());
    }

    private void updatePatient() throws HospitalException {
        String id = prompt("Patient ID");
        hospital.getPatient(id); // fail fast if it does not exist
        System.out.println("Leave a field blank to keep its current value.");

        String name = optional(prompt("New name"));
        String phoneRaw = optional(prompt("New phone"));
        String ailment = optional(prompt("New ailment"));
        String admittedRaw = optional(prompt("Admitted? (y/n)"));

        String phone = phoneRaw == null ? null : InputValidator.requirePhone(phoneRaw);
        Boolean admitted = admittedRaw == null ? null : InputValidator.requireYesNo(admittedRaw);

        hospital.updatePatient(id, name, phone, ailment, admitted);
        System.out.println("Patient updated.");
    }

    private void addDoctor() throws HospitalException {
        String name = InputValidator.requireText(prompt("Name"), "Name");
        int age = InputValidator.requireAge(prompt("Age"));
        String gender = InputValidator.requireGender(prompt("Gender (M/F/O)"));
        String phone = InputValidator.requirePhone(prompt("Phone (10 digits)"));
        String spec = InputValidator.requireText(prompt("Specialization"), "Specialization");
        double fee = InputValidator.requireAmount(prompt("Consultation fee"), "Fee");
        boolean available = InputValidator.requireYesNo(prompt("Available? (y/n)"));

        Doctor d = hospital.addDoctor(name, age, gender, phone, spec, fee, available);
        System.out.println("Doctor added with ID " + d.getId());
    }

    private void updateDoctor() throws HospitalException {
        String id = prompt("Doctor ID");
        hospital.getDoctor(id);
        System.out.println("Leave a field blank to keep its current value.");

        String name = optional(prompt("New name"));
        String phoneRaw = optional(prompt("New phone"));
        String feeRaw = optional(prompt("New consultation fee"));
        String availRaw = optional(prompt("Available? (y/n)"));

        String phone = phoneRaw == null ? null : InputValidator.requirePhone(phoneRaw);
        Double fee = feeRaw == null ? null : InputValidator.requireAmount(feeRaw, "Fee");
        Boolean available = availRaw == null ? null : InputValidator.requireYesNo(availRaw);

        hospital.updateDoctor(id, name, phone, fee, available);
        System.out.println("Doctor updated.");
    }

    private void bookAppointment() throws HospitalException {
        String patientId = prompt("Patient ID");
        String doctorId = prompt("Doctor ID");
        LocalDate date = InputValidator.requireFutureDate(prompt("Date (yyyy-MM-dd)"));
        String slot = InputValidator.requireText(prompt("Slot (e.g. 10:00-10:30)"), "Slot");

        Appointment a = hospital.bookAppointment(patientId, doctorId, date, slot);
        System.out.println("Appointment booked with ID " + a.getId());
    }

    private void generateBill() throws HospitalException {
        String appointmentId = prompt("Appointment ID (must be COMPLETED)");
        double room = InputValidator.requireAmount(prompt("Room charges"), "Room charges");
        double medicine = InputValidator.requireAmount(prompt("Medicine charges"), "Medicine charges");

        Bill bill = hospital.generateBill(appointmentId, room, medicine);
        System.out.print(bill.toReceipt());
    }

    private void showSummary() {
        System.out.println();
        System.out.println("--------- SUMMARY ---------");
        System.out.printf(" Patients registered : %d%n", hospital.listPatients().size());
        System.out.printf(" Currently admitted  : %d%n", hospital.countAdmitted());
        System.out.printf(" Doctors on staff    : %d%n", hospital.listDoctors().size());
        System.out.printf(" Scheduled           : %d%n",
                hospital.countByStatus(Appointment.Status.SCHEDULED));
        System.out.printf(" Completed           : %d%n",
                hospital.countByStatus(Appointment.Status.COMPLETED));
        System.out.printf(" Cancelled           : %d%n",
                hospital.countByStatus(Appointment.Status.CANCELLED));
        System.out.printf(" Bills raised        : %d%n", hospital.listBills().size());
        System.out.printf(" Total outstanding   : %.2f%n", hospital.totalOutstanding());
    }

    // ---------------- Printing helpers ----------------

    private void printPatients(List<Patient> list) {
        if (list.isEmpty()) {
            System.out.println("No matching patients.");
            return;
        }
        System.out.printf("%n%-6s %-20s %-5s %-8s %-12s %-18s %-5s %s%n",
                "ID", "NAME", "AGE", "GENDER", "PHONE", "AILMENT", "BG", "STATUS");
        list.forEach(System.out::println);
    }

    private void printDoctors(List<Doctor> list) {
        if (list.isEmpty()) {
            System.out.println("No matching doctors.");
            return;
        }
        System.out.printf("%n%-6s %-20s %-5s %-8s %-12s %-16s %10s %s%n",
                "ID", "NAME", "AGE", "GENDER", "PHONE", "SPECIALIZATION", "FEE", "STATUS");
        list.forEach(System.out::println);
    }

    private void printAppointments(List<Appointment> list) {
        if (list.isEmpty()) {
            System.out.println("No matching appointments.");
            return;
        }
        System.out.printf("%n%-6s %-6s %-6s %-12s %-12s %s%n",
                "ID", "PATIENT", "DOCTOR", "DATE", "SLOT", "STATUS");
        list.forEach(System.out::println);
    }

    private void printBills(List<Bill> list) {
        if (list.isEmpty()) {
            System.out.println("No bills raised yet.");
            return;
        }
        System.out.printf("%n%-6s %-6s %-6s %12s %s%n",
                "ID", "PATIENT", "APPT", "TOTAL", "STATUS");
        list.forEach(System.out::println);
    }

    // ---------------- Input helpers ----------------

    private String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.hasNextLine() ? scanner.nextLine() : "0";
    }

    /** Returns null for a blank entry, which the update methods read as "no change". */
    private String optional(String raw) {
        return (raw == null || raw.trim().isEmpty()) ? null : raw.trim();
    }
}
