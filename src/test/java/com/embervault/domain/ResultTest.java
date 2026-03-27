package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Result} sealed interface (ADR-0017).
 *
 * <p>Verifies success/failure creation, accessor methods, pattern matching
 * via switch expressions, and edge cases.</p>
 */
class ResultTest {

    @Nested
    @DisplayName("Success")
    class SuccessTests {

        @Test
        @DisplayName("factory method creates a Success instance")
        void factoryMethodCreatesSuccess() {
            Result<String> result = Result.success("hello");
            assertInstanceOf(Result.Success.class, result);
        }

        @Test
        @DisplayName("isSuccess returns true")
        void isSuccessReturnsTrue() {
            Result<String> result = Result.success("hello");
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("isFailure returns false")
        void isFailureReturnsFalse() {
            Result<String> result = Result.success("hello");
            assertFalse(result.isFailure());
        }

        @Test
        @DisplayName("getValue returns the wrapped value")
        void getValueReturnsWrappedValue() {
            Result<Integer> result = Result.success(42);
            assertEquals(42, result.getValue());
        }

        @Test
        @DisplayName("getError returns null for success")
        void getErrorReturnsNull() {
            Result<String> result = Result.success("hello");
            assertNull(result.getError());
        }

        @Test
        @DisplayName("allows null as a successful value")
        void allowsNullValue() {
            Result<String> result = Result.success(null);
            assertTrue(result.isSuccess());
            assertNull(result.getValue());
        }

        @Test
        @DisplayName("Success record exposes value via accessor")
        void recordAccessor() {
            var success = new Result.Success<>("data");
            assertEquals("data", success.value());
        }
    }

    @Nested
    @DisplayName("Failure")
    class FailureTests {

        @Test
        @DisplayName("factory method with message creates a Failure instance")
        void factoryMethodCreatesFailure() {
            Result<String> result = Result.failure("something went wrong");
            assertInstanceOf(Result.Failure.class, result);
        }

        @Test
        @DisplayName("isSuccess returns false")
        void isSuccessReturnsFalse() {
            Result<String> result = Result.failure("error");
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("isFailure returns true")
        void isFailureReturnsTrue() {
            Result<String> result = Result.failure("error");
            assertTrue(result.isFailure());
        }

        @Test
        @DisplayName("getError returns the error message")
        void getErrorReturnsMessage() {
            Result<String> result = Result.failure("bad input");
            assertEquals("bad input", result.getError());
        }

        @Test
        @DisplayName("getValue returns null for failure")
        void getValueReturnsNull() {
            Result<String> result = Result.failure("error");
            assertNull(result.getValue());
        }

        @Test
        @DisplayName("factory method with message and cause preserves both")
        void factoryMethodWithCause() {
            var cause = new RuntimeException("root cause");
            Result<String> result = Result.failure("wrapped", cause);

            assertInstanceOf(Result.Failure.class, result);
            assertEquals("wrapped", result.getError());

            var failure = (Result.Failure<String>) result;
            assertSame(cause, failure.cause());
        }

        @Test
        @DisplayName("failure without cause has null cause")
        void failureWithoutCauseHasNullCause() {
            Result<String> result = Result.failure("no cause");
            var failure = (Result.Failure<String>) result;
            assertNull(failure.cause());
        }

        @Test
        @DisplayName("Failure record exposes message via accessor")
        void recordMessageAccessor() {
            var failure = new Result.Failure<String>("msg", null);
            assertEquals("msg", failure.message());
        }

        @Test
        @DisplayName("factory method rejects null message")
        void rejectsNullMessage() {
            assertThrows(NullPointerException.class,
                    () -> Result.failure(null));
        }

        @Test
        @DisplayName("factory method with cause rejects null message")
        void rejectsNullMessageWithCause() {
            assertThrows(NullPointerException.class,
                    () -> Result.failure(null, new RuntimeException()));
        }
    }

    @Nested
    @DisplayName("Pattern matching")
    class PatternMatchingTests {

        @Test
        @DisplayName("switch expression matches Success")
        void switchMatchesSuccess() {
            Result<String> result = Result.success("matched");

            String output = switch (result) {
                case Result.Success<String> s -> "ok: " + s.value();
                case Result.Failure<String> f -> "err: " + f.message();
            };

            assertEquals("ok: matched", output);
        }

        @Test
        @DisplayName("switch expression matches Failure")
        void switchMatchesFailure() {
            Result<String> result = Result.failure("broken");

            String output = switch (result) {
                case Result.Success<String> s -> "ok: " + s.value();
                case Result.Failure<String> f -> "err: " + f.message();
            };

            assertEquals("err: broken", output);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Success with different generic types")
        void successWithDifferentTypes() {
            Result<Integer> intResult = Result.success(100);
            Result<String> strResult = Result.success("text");

            assertEquals(100, intResult.getValue());
            assertEquals("text", strResult.getValue());
        }

        @Test
        @DisplayName("sealed hierarchy is exhaustive — only Success and Failure exist")
        void sealedHierarchyIsExhaustive() {
            Result<String> result = Result.success("test");

            // This switch compiles without a default branch because the
            // sealed hierarchy is exhaustive — proving only two subtypes exist.
            boolean handled = switch (result) {
                case Result.Success<String> s -> true;
                case Result.Failure<String> f -> true;
            };

            assertTrue(handled);
        }
    }
}
