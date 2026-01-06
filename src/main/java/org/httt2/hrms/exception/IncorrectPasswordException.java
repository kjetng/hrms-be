package org.httt2.hrms.exception;

/**
 * Exception thrown when the current password is incorrect.
 */
public class IncorrectPasswordException extends ApplicationException {

    public IncorrectPasswordException(String message) {
        super(message);
    }
}

