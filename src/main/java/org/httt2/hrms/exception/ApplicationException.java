package org.httt2.hrms.exception;

/**
 * Base exception class for application-specific exceptions.
 * All custom business logic exceptions should extend this class.
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
