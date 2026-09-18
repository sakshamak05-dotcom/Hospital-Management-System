package com.hospital.model;

import com.hospital.exception.HospitalException;

/**
 * A doctor on staff. consultationFee feeds directly into billing, so it is a
 * double rather than a String.
 */
public class Doctor extends Person {

    private String specialization;
    private double consultationFee;
    private boolean available;

    public Doctor(String id, String name, int age, String gender, String phone,
                  String specialization, double consultationFee, boolean available) {
        super(id, name, age, gender, phone);
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.available = available;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String getRole() {
        return "DOCTOR";
    }

    @Override
    public String toCsv() {
        return String.join(",", getId(), getName(), String.valueOf(getAge()),
                getGender(), getPhone(), specialization,
                String.valueOf(consultationFee), String.valueOf(available));
    }

    public static Doctor fromCsv(String line) throws HospitalException {
        String[] p = line.split(",", -1);
        if (p.length != 8) {
            throw new HospitalException("Malformed doctor record: " + line);
        }
        try {
            return new Doctor(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4],
                    p[5], Double.parseDouble(p[6]), Boolean.parseBoolean(p[7]));
        } catch (NumberFormatException e) {
            throw new HospitalException("Bad number in doctor record: " + line, e);
        }
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" %-16s %10.2f %s",
                specialization, consultationFee, available ? "AVAILABLE" : "ON LEAVE");
    }
}
