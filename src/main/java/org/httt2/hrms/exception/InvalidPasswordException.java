package org.httt2.hrms.exception;

/**
 * Exception thrown when password validation fails.
 */
public class InvalidPasswordException extends ApplicationException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}

