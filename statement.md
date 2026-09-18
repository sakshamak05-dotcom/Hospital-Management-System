# Problem Statement

## Problem Statement

Small and mid-sized clinics still run patient registers, doctor rosters and
billing on paper or on disconnected spreadsheets. Because the three are kept
separately, the same information is written down more than once and the copies
drift apart. Common failures are double-booking a doctor into one slot,
discharging a patient whose bill was never raised, and being unable to answer a
simple question such as which patients share a blood group without reading every
page of the register.

This project addresses that by keeping patients, doctors, appointments and bills
in one system where the relationships between them are enforced by the program
rather than by staff discipline.

## Scope

**In scope**

- Registration and lifecycle management of patient records
- Maintenance of the doctor roster, including specialization, fee and duty status
- Appointment booking with conflict detection, and cancellation or completion
- Itemised billing against completed appointments, with tax and payment status
- Search across patients and doctors, and appointment history per patient
- Summary reporting on headcounts, appointment states and outstanding dues
- Persistence of all records to local CSV files between runs

**Out of scope**

- Multi-user access, authentication and role-based permissions
- Network or client-server operation; the system runs on a single machine
- Graphical or web interface; interaction is entirely through the terminal
- Clinical records such as prescriptions, test results or imaging
- Payment gateway integration; the system only records payment status

## Target Users

**Front-desk and reception staff** are the primary users. They register walk-in
patients, look up existing records, and book appointments against the roster.

**Billing staff** raise invoices once a consultation is completed and record
payment against them.

**Administrators** use the roster and summary functions to keep doctor details
current and to see occupancy and outstanding dues at a glance.

The system assumes a single trusted operator at a time rather than concurrent
users, which is why authentication is deliberately left out of scope.

## High-Level Features

1. **Patient management** — create, list, view, update and delete patient
   records, with deletion blocked while a scheduled appointment still refers to
   the patient.

2. **Doctor management** — maintain the roster, with consultation fee and
   availability. Doctors marked unavailable cannot be booked.

3. **Appointment management** — book a patient with a doctor on a date and slot,
   rejecting double-bookings and past dates; then complete or cancel.

4. **Billing** — generate a one-time itemised invoice for a completed
   appointment, combining consultation, room and medicine charges with tax, and
   track payment.

5. **Search and reporting** — case-insensitive search across records, appointment
   history per patient, day-wise appointment lists, and a summary of outstanding
   dues.

6. **Validation and error handling** — all input is validated before it reaches
   the service layer; failures raise a custom checked exception that the
   interface reports without terminating.

7. **Persistent storage** — records are written to CSV after every change and
   reloaded at startup.
