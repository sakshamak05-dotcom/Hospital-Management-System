package com.hospital.exception;

/**
 * Checked exception used for all recoverable domain errors, e.g. a lookup that
 * finds nothing, a duplicate ID, or a rejected value from the validator.
 *
 * It is checked (extends Exception, not RuntimeException) on purpose: the CLI
 * layer is expected to catch these and show the user a message rather than
 * crash, so the compiler forcing a catch block is helpful here.
 */
public class HospitalException extends Exception {

    public HospitalException(String message) {
        super(message);
    }

    public HospitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
