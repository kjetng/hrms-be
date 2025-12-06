package org.httt2.hrms.exception;

/**
 * Exception thrown when attempting to register a user with an email
 * that already exists in the system.
 */
public class EmailAlreadyExistsException extends ApplicationException {

    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }
}
