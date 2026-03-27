package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the domain exception hierarchy (ADR-0016).
 *
 * <p>Verifies that every domain exception carries messages and causes
 * correctly and extends the expected base types.</p>
 */
class DomainExceptionTest {

    @Nested
    @DisplayName("EntityNotFoundException")
    class EntityNotFoundExceptionTests {

        @Test
        @DisplayName("carries the detail message")
        void shouldCarryMessage() {
            var exception = new EntityNotFoundException("Vault not found");
            assertEquals("Vault not found", exception.getMessage());
        }

        @Test
        @DisplayName("carries the detail message and cause")
        void shouldCarryMessageAndCause() {
            var cause = new RuntimeException("underlying error");
            var exception = new EntityNotFoundException("Vault not found", cause);

            assertEquals("Vault not found", exception.getMessage());
            assertSame(cause, exception.getCause());
        }

        @Test
        @DisplayName("has null cause when constructed with message only")
        void shouldHaveNullCauseWhenNotProvided() {
            var exception = new EntityNotFoundException("Vault not found");
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("extends DomainException")
        void shouldExtendDomainException() {
            var exception = new EntityNotFoundException("not found");
            assertInstanceOf(DomainException.class, exception);
        }

        @Test
        @DisplayName("extends RuntimeException (unchecked)")
        void shouldExtendRuntimeException() {
            var exception = new EntityNotFoundException("not found");
            assertInstanceOf(RuntimeException.class, exception);
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTests {

        @Test
        @DisplayName("carries the detail message")
        void shouldCarryMessage() {
            var exception = new ValidationException("Name must not be blank");
            assertEquals("Name must not be blank", exception.getMessage());
        }

        @Test
        @DisplayName("carries the detail message and cause")
        void shouldCarryMessageAndCause() {
            var cause = new IllegalArgumentException("bad input");
            var exception = new ValidationException("Name must not be blank", cause);

            assertEquals("Name must not be blank", exception.getMessage());
            assertSame(cause, exception.getCause());
        }

        @Test
        @DisplayName("has null cause when constructed with message only")
        void shouldHaveNullCauseWhenNotProvided() {
            var exception = new ValidationException("Name must not be blank");
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("extends DomainException")
        void shouldExtendDomainException() {
            var exception = new ValidationException("invalid");
            assertInstanceOf(DomainException.class, exception);
        }

        @Test
        @DisplayName("extends RuntimeException (unchecked)")
        void shouldExtendRuntimeException() {
            var exception = new ValidationException("invalid");
            assertInstanceOf(RuntimeException.class, exception);
        }
    }
}
