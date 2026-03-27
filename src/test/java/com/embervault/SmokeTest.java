package com.embervault;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke test to verify JUnit 5 is correctly configured and tests are discovered
 * by the Maven Surefire plugin.
 */
class SmokeTest {

    @Test
    @DisplayName("JUnit 5 is configured and test discovery works")
    void junitFiveIsConfigured() {
        assertTrue(true, "JUnit 5 test was discovered and executed");
    }
}
