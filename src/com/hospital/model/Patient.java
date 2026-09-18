package com.hospital.model;

import com.hospital.exception.HospitalException;

/**
 * A patient registered with the hospital. Adds the medical fields that a plain
 * Person does not have.
 */
public class Patient extends Person {

    private String ailment;
    private String bloodGroup;
    private boolean admitted;

    public Patient(String id, String name, int age, String gender, String phone,
                   String ailment, String bloodGroup, boolean admitted) {
        super(id, name, age, gender, phone);
        this.ailment = ailment;
        this.bloodGroup = bloodGroup;
        this.admitted = admitted;
    }

    public String getAilment() {
        return ailment;
    }

    public void setAilment(String ailment) {
        this.ailment = ailment;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public boolean isAdmitted() {
        return admitted;
    }

    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }

    @Override
    public String getRole() {
        return "PATIENT";
    }

    @Override
    public String toCsv() {
        return String.join(",", getId(), getName(), String.valueOf(getAge()),
                getGender(), getPhone(), ailment, bloodGroup, String.valueOf(admitted));
    }

    /**
     * Rebuilds a Patient from one CSV line. Kept next to toCsv so that if the
     * field order changes, both halves are visible in the same file.
     */
    public static Patient fromCsv(String line) throws HospitalException {
        String[] p = line.split(",", -1);
        if (p.length != 8) {
            throw new HospitalException("Malformed patient record: " + line);
        }
        try {
            return new Patient(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4],
                    p[5], p[6], Boolean.parseBoolean(p[7]));
        } catch (NumberFormatException e) {
            throw new HospitalException("Bad age in patient record: " + line, e);
        }
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" %-18s %-5s %s",
                ailment, bloodGroup, admitted ? "ADMITTED" : "OUTPATIENT");
    }
}
