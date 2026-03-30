package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UuidGenerator}.
 */
class UuidGeneratorTest {

    @Test
    @DisplayName("generated UUID has version 7")
    void generate_shouldReturnVersion7() {
        UUID id = UuidGenerator.generate();
        assertEquals(7, id.version(),
                "UUID should be version 7");
    }

    @Test
    @DisplayName("generated UUID has variant 2 (RFC 9562)")
    void generate_shouldHaveCorrectVariant() {
        UUID id = UuidGenerator.generate();
        assertEquals(2, id.variant(),
                "UUID should have IETF variant");
    }

    @Test
    @DisplayName("sequential UUIDs are monotonically ordered")
    void generate_shouldBeMonotonicallyOrdered() {
        UUID a = UuidGenerator.generate();
        UUID b = UuidGenerator.generate();
        assertTrue(a.compareTo(b) < 0,
                "Sequential UUIDs should be ordered: "
                        + a + " < " + b);
    }

    @Test
    @DisplayName("generated UUIDs are unique")
    void generate_shouldBeUnique() {
        UUID a = UuidGenerator.generate();
        UUID b = UuidGenerator.generate();
        assertTrue(!a.equals(b),
                "UUIDs should be unique");
    }
}
