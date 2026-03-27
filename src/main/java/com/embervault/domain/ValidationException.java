package com.embervault.domain;

/**
 * Thrown when a domain invariant or input validation rule is violated.
 *
 * <p>This exception indicates that the caller supplied data that does not
 * satisfy the domain's validation constraints. See ADR-0016 for the error
 * handling strategy.</p>
 */
public class ValidationException extends DomainException {

    /**
     * Creates a validation exception with the specified detail message.
     *
     * @param message the detail message describing the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates a validation exception with the specified detail message and cause.
     *
     * @param message the detail message describing the validation failure
     * @param cause   the underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
