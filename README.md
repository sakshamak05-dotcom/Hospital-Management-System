# Hospital Management System

A command-line hospital management system written in core Java. It manages patient
records, doctor records, appointment scheduling and billing, with all data persisted
to CSV files on disk so records survive between runs.

Built for the VITyarthi *Programming in Java* flipped-course project.

---

## Overview

The system replaces paper registers with a single terminal application. Front-desk
staff register patients, maintain the doctor roster, book appointments against a
doctor's free slots, and raise itemised bills once a consultation is complete.

The design separates three layers:

| Layer | Package | Responsibility |
|---|---|---|
| Presentation | `com.hospital` | Menus, input collection, output formatting |
| Service | `com.hospital.service` | All business rules and validation logic |
| Storage | `com.hospital.storage` | Reading and writing CSV files |

Model classes live in `com.hospital.model`, the custom exception in
`com.hospital.exception`, and reusable validation in `com.hospital.util`.

---

## Features

**Patient management** — register patients with demographic and medical details,
list all records, look up by ID, update selected fields, and delete. Deletion is
blocked while a scheduled appointment still references the patient.

**Doctor management** — maintain the roster with specialization, consultation fee
and duty status. Doctors marked on leave cannot be booked.

**Appointment management** — book a patient with a doctor on a date and slot.
Booking is refused if the doctor is on leave or already has that slot taken.
Appointments move through `SCHEDULED` → `COMPLETED` or `CANCELLED`.

**Billing** — generate an itemised invoice for a completed appointment, combining
the consultation fee, room charges and medicine charges with 5% tax. Each
appointment can be billed only once. Bills can be marked paid, and outstanding
dues are totalled on demand.

**Search** — case-insensitive substring search across patients (name, ailment,
blood group) and doctors (name, specialization), plus appointment history per
patient and a day-wise appointment list.

**Reporting** — a summary screen with headcounts, appointment status breakdown
and total outstanding dues.

**Input validation and error handling** — every field is validated before it
reaches the service layer. Invalid input raises a `HospitalException`, which the
menu catches and reports without terminating the program.

**File storage** — data is written after every change to `data/patients.csv`,
`data/doctors.csv`, `data/appointments.csv` and `data/bills.csv`.

---

## Technologies Used

- **Java SE 17 or newer** (uses arrow-form `switch`)
- **Core Java only** — `java.util` collections, `java.nio.file` for I/O,
  `java.time.LocalDate` for dates
- **No external libraries, no build tool** — compiles with `javac` alone
- **Git** for version control

---

## Project Structure

```
hospital-management-system/
├── src/
│   └── com/
│       └── hospital/
│           ├── Main.java                  # CLI entry point and menus
│           ├── model/
│           │   ├── Person.java            # abstract base class
│           │   ├── Patient.java
│           │   ├── Doctor.java
│           │   ├── Appointment.java
│           │   └── Bill.java
│           ├── service/
│           │   └── Hospital.java          # business logic and CRUD
│           ├── storage/
│           │   └── FileManager.java       # CSV persistence
│           ├── util/
│           │   └── InputValidator.java    # validation rules
│           ├── exception/
│           │   └── HospitalException.java # custom checked exception
│           └── test/
│               └── HospitalTest.java      # validation test suite
├── data/                                  # created on first run
├── run.sh
├── test.sh
├── .gitignore
├── statement.md
└── README.md
```

---

## Prerequisites

A JDK version 17 or newer must be installed and on your `PATH`. Verify with:

```bash
java -version
javac -version
```

Both commands must print a version. If `javac` is missing you have a JRE rather
than a JDK — install a full JDK (for example Temurin 17 or OpenJDK 21).

No other dependencies are required. Nothing needs to be downloaded or configured.

---

## Setup and Run

### 1. Clone the repository

```bash
git clone https://github.com/{your-username}/{your-repo-name}.git
cd {your-repo-name}
```

### 2. Compile

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

On Windows PowerShell:

```powershell
mkdir out -Force
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

### 3. Run

```bash
java -cp out com.hospital.Main
```

On Linux and macOS the two steps above are wrapped in a script:

```bash
./run.sh
```

The `data/` directory is created automatically on first launch. The application
opens on the main menu; enter the number beside an option and press Enter.

---

## Testing

The project ships with a dependency-free test suite covering input validation,
CRUD operations, appointment rules, billing arithmetic, search and file
persistence.

```bash
javac -d out $(find src -name "*.java")
java -cp out com.hospital.test.HospitalTest
```

or:

```bash
./test.sh
```

Each assertion prints `PASS` or `FAIL`, followed by a total. The process exits
with code `1` if any assertion fails, so the suite can be used in a CI check.

Tests write to a separate `data-test/` directory and never touch `data/`.

---

## Walkthrough

A short sequence that exercises the whole system:

1. `2` → `1` — add a doctor (note the generated ID, e.g. `D001`)
2. `1` → `1` — register a patient (note the ID, e.g. `P001`)
3. `3` → `1` — book an appointment using both IDs, a future date, and a slot
4. `3` → `1` — try booking the same doctor, date and slot again; it is refused
5. `3` → `3` — mark the appointment `COMPLETED`
6. `4` → `1` — generate the bill; the invoice prints with tax applied
7. `4` → `1` — try billing the same appointment again; it is refused
8. `6` — view the summary report
9. `0` — exit, then relaunch and list patients to confirm data persisted

---

## Data Format

Each entity is stored as one CSV record per line, without a header row.

| File | Fields |
|---|---|
| `patients.csv` | id, name, age, gender, phone, ailment, bloodGroup, admitted |
| `doctors.csv` | id, name, age, gender, phone, specialization, fee, available |
| `appointments.csv` | id, patientId, doctorId, date, slot, status |
| `bills.csv` | id, patientId, appointmentId, consultationFee, roomCharges, medicineCharges, paid |

Because the comma is the delimiter, the validator rejects any text field that
contains one.

---

## Troubleshooting

**`javac: command not found`** — a JDK is not installed or not on `PATH`.

**`error: invalid source release`** — your JDK is older than 17. Install a newer
JDK, or replace the arrow-form `switch` statements in `Main.java` with classic
`switch` blocks using `case "1": ... break;`.

**`Malformed patient record`** — a CSV file in `data/` was hand-edited into an
invalid shape. Correct the line or delete the file to start fresh.
