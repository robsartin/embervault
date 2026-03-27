package com.embervault.domain;

/**
 * Thrown when a requested entity cannot be found.
 *
 * <p>This exception signals that a lookup by identifier yielded no result,
 * which is a domain-level concern (the entity simply does not exist).
 * See ADR-0016 for the error handling strategy.</p>
 */
public class EntityNotFoundException extends DomainException {

    /**
     * Creates an exception indicating that an entity was not found.
     *
     * @param message the detail message describing which entity was missing
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception indicating that an entity was not found, with an
     * underlying cause.
     *
     * @param message the detail message describing which entity was missing
     * @param cause   the underlying cause
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
