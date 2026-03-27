package com.embervault.domain;

import java.util.Objects;

/**
 * A sealed result type representing either a successful value or a failure.
 *
 * <p>This type makes success and failure explicit in method signatures,
 * eliminating hidden control flow paths that exceptions introduce for
 * expected outcomes. Use pattern matching (switch expressions) to handle
 * both cases exhaustively at compile time.</p>
 *
 * <p>Exceptions remain appropriate for truly exceptional/unexpected
 * situations (see ADR-0016). {@code Result} is intended for expected
 * outcomes where callers must explicitly handle both success and failure
 * (see ADR-0017).</p>
 *
 * <p>Example usage with Java 25 pattern matching:</p>
 * <pre>{@code
 * Result<Note> result = noteService.findById(id);
 * switch (result) {
 *     case Result.Success<Note> s -> display(s.value());
 *     case Result.Failure<Note> f -> showError(f.message());
 * }
 * }</pre>
 *
 * @param <T> the type of the successful value
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /**
     * Creates a successful result wrapping the given value.
     *
     * @param value the successful value (may be {@code null})
     * @param <T>   the value type
     * @return a {@link Success} containing the value
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failure result with the given error message.
     *
     * @param message the error message (must not be {@code null})
     * @param <T>     the value type of the result
     * @return a {@link Failure} containing the message
     */
    static <T> Result<T> failure(String message) {
        Objects.requireNonNull(message, "message must not be null");
        return new Failure<>(message, null);
    }

    /**
     * Creates a failure result with the given error message and cause.
     *
     * @param message the error message (must not be {@code null})
     * @param cause   the underlying exception that caused the failure
     * @param <T>     the value type of the result
     * @return a {@link Failure} containing the message and cause
     */
    static <T> Result<T> failure(String message, Throwable cause) {
        Objects.requireNonNull(message, "message must not be null");
        return new Failure<>(message, cause);
    }

    /**
     * Returns {@code true} if this result represents a success.
     *
     * @return {@code true} for {@link Success}, {@code false} for {@link Failure}
     */
    boolean isSuccess();

    /**
     * Returns {@code true} if this result represents a failure.
     *
     * @return {@code true} for {@link Failure}, {@code false} for {@link Success}
     */
    boolean isFailure();

    /**
     * Returns the successful value, or {@code null} if this is a failure.
     *
     * <p>Prefer pattern matching over this accessor for type-safe handling.</p>
     *
     * @return the value, or {@code null}
     */
    T getValue();

    /**
     * Returns the error message, or {@code null} if this is a success.
     *
     * <p>Prefer pattern matching over this accessor for type-safe handling.</p>
     *
     * @return the error message, or {@code null}
     */
    String getError();

    /**
     * A successful result carrying a value.
     *
     * @param value the successful value
     * @param <T>   the value type
     */
    record Success<T>(T value) implements Result<T> {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public String getError() {
            return null;
        }
    }

    /**
     * A failed result carrying an error message and an optional cause.
     *
     * @param message the error message
     * @param cause   the underlying exception, or {@code null}
     * @param <T>     the value type (unused, but preserves generic compatibility)
     */
    record Failure<T>(String message, Throwable cause) implements Result<T> {

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public String getError() {
            return message;
        }
    }
}
