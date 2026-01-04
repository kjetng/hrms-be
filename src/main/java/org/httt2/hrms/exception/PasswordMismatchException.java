package org.httt2.hrms.exception;

/**
 * Exception thrown when passwords do not match.
 */
public class PasswordMismatchException extends ApplicationException {

    public PasswordMismatchException(String message) {
        super(message);
    }
}

