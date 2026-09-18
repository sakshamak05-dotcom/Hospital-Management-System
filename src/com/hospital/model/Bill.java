package com.hospital.model;

import com.hospital.exception.HospitalException;

/**
 * Charges raised against a completed appointment.
 *
 * The total is deliberately NOT stored as a field — it is derived in
 * getTotal(). Storing it would allow the stored total and the component charges
 * to drift apart if one is edited without the other.
 */
public class Bill {

    /** Applied to the sum of all charges. 5% here; change in one place. */
    public static final double TAX_RATE = 0.05;

    private final String id;
    private final String patientId;
    private final String appointmentId;
    private double consultationFee;
    private double roomCharges;
    private double medicineCharges;
    private boolean paid;

    public Bill(String id, String patientId, String appointmentId,
                double consultationFee, double roomCharges,
                double medicineCharges, boolean paid) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.roomCharges = roomCharges;
        this.medicineCharges = medicineCharges;
        this.paid = paid;
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public double getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(double roomCharges) {
        this.roomCharges = roomCharges;
    }

    public double getMedicineCharges() {
        return medicineCharges;
    }

    public void setMedicineCharges(double medicineCharges) {
        this.medicineCharges = medicineCharges;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public double getSubtotal() {
        return consultationFee + roomCharges + medicineCharges;
    }

    public double getTax() {
        return getSubtotal() * TAX_RATE;
    }

    public double getTotal() {
        return getSubtotal() + getTax();
    }

    public String toCsv() {
        return String.join(",", id, patientId, appointmentId,
                String.valueOf(consultationFee), String.valueOf(roomCharges),
                String.valueOf(medicineCharges), String.valueOf(paid));
    }

    public static Bill fromCsv(String line) throws HospitalException {
        String[] p = line.split(",", -1);
        if (p.length != 7) {
            throw new HospitalException("Malformed bill record: " + line);
        }
        try {
            return new Bill(p[0], p[1], p[2], Double.parseDouble(p[3]),
                    Double.parseDouble(p[4]), Double.parseDouble(p[5]),
                    Boolean.parseBoolean(p[6]));
        } catch (NumberFormatException e) {
            throw new HospitalException("Bad amount in bill record: " + line, e);
        }
    }

    /** Multi-line receipt used by the billing menu. */
    public String toReceipt() {
        return String.format(
                "%n---------------- INVOICE ----------------%n"
                + " Bill ID        : %s%n"
                + " Patient ID     : %s%n"
                + " Appointment ID : %s%n"
                + "-----------------------------------------%n"
                + " Consultation   : %12.2f%n"
                + " Room charges   : %12.2f%n"
                + " Medicines      : %12.2f%n"
                + " Subtotal       : %12.2f%n"
                + " Tax (%.0f%%)      : %12.2f%n"
                + " TOTAL          : %12.2f%n"
                + " Status         : %s%n"
                + "-----------------------------------------%n",
                id, patientId, appointmentId, consultationFee, roomCharges,
                medicineCharges, getSubtotal(), TAX_RATE * 100, getTax(),
                getTotal(), paid ? "PAID" : "UNPAID");
    }

    @Override
    public String toString() {
        return String.format("%-6s %-6s %-6s %12.2f %s",
                id, patientId, appointmentId, getTotal(), paid ? "PAID" : "UNPAID");
    }
}
