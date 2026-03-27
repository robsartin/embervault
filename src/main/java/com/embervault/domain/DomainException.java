package com.embervault.domain;

/**
 * Base class for all domain-originated exceptions.
 *
 * <p>All custom exceptions within the domain layer must extend this class.
 * This constraint is enforced by an ArchUnit rule (see ADR-0016).
 *
 * <p>{@code DomainException} is an unchecked exception (extends
 * {@link RuntimeException}) so that it can propagate through intermediate
 * layers without polluting method signatures with checked-exception
 * declarations.</p>
 */
public abstract class DomainException extends RuntimeException {

    /**
     * Creates a domain exception with the specified detail message.
     *
     * @param message the detail message
     */
    protected DomainException(String message) {
        super(message);
    }

    /**
     * Creates a domain exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
