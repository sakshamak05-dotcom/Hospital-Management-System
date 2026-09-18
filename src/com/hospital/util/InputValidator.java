package com.hospital.util;

import com.hospital.exception.HospitalException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Central place for every validation rule. Keeping them here rather than
 * scattered through the menu code means a rule is defined once and the CLI
 * stays readable.
 *
 * All methods are static and the class cannot be instantiated.
 */
public final class InputValidator {

    private InputValidator() {
        // utility class — no instances
    }

    /** Rejects null, blank, and anything containing a comma (CSV delimiter). */
    public static String requireText(String value, String fieldName) throws HospitalException {
        if (value == null || value.trim().isEmpty()) {
            throw new HospitalException(fieldName + " cannot be empty.");
        }
        if (value.contains(",")) {
            throw new HospitalException(fieldName + " cannot contain a comma.");
        }
        return value.trim();
    }

    public static int requireAge(String raw) throws HospitalException {
        int age;
        try {
            age = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new HospitalException("Age must be a whole number.", e);
        }
        if (age < 0 || age > 120) {
            throw new HospitalException("Age must be between 0 and 120.");
        }
        return age;
    }

    /** Accepts exactly 10 digits — the Indian mobile format used here. */
    public static String requirePhone(String raw) throws HospitalException {
        String phone = requireText(raw, "Phone");
        if (!phone.matches("\\d{10}")) {
            throw new HospitalException("Phone must be exactly 10 digits.");
        }
        return phone;
    }

    public static String requireGender(String raw) throws HospitalException {
        String g = requireText(raw, "Gender").toUpperCase();
        if (!g.equals("M") && !g.equals("F") && !g.equals("O")) {
            throw new HospitalException("Gender must be M, F or O.");
        }
        return g;
    }

    public static String requireBloodGroup(String raw) throws HospitalException {
        String bg = requireText(raw, "Blood group").toUpperCase();
        if (!bg.matches("(A|B|AB|O)[+-]")) {
            throw new HospitalException("Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-.");
        }
        return bg;
    }

    public static double requireAmount(String raw, String fieldName) throws HospitalException {
        double amount;
        try {
            amount = Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new HospitalException(fieldName + " must be a number.", e);
        }
        if (amount < 0) {
            throw new HospitalException(fieldName + " cannot be negative.");
        }
        return amount;
    }

    /** Parses yyyy-MM-dd and refuses dates in the past. */
    public static LocalDate requireFutureDate(String raw) throws HospitalException {
        LocalDate date;
        try {
            date = LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new HospitalException("Date must be in yyyy-MM-dd format, e.g. 2026-10-05.", e);
        }
        if (date.isBefore(LocalDate.now())) {
            throw new HospitalException("Appointment date cannot be in the past.");
        }
        return date;
    }

    public static boolean requireYesNo(String raw) throws HospitalException {
        String v = requireText(raw, "Answer").toLowerCase();
        if (v.equals("y") || v.equals("yes")) {
            return true;
        }
        if (v.equals("n") || v.equals("no")) {
            return false;
        }
        throw new HospitalException("Please answer y or n.");
    }
}
