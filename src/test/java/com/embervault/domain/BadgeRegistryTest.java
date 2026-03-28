package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BadgeRegistryTest {

    @Test
    @DisplayName("getBadgeSymbol() returns star symbol for 'star'")
    void getBadgeSymbol_shouldReturnStarSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("star");

        assertTrue(symbol.isPresent());
        assertEquals("\u2B50", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns flag symbol for 'flag'")
    void getBadgeSymbol_shouldReturnFlagSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("flag");

        assertTrue(symbol.isPresent());
        assertEquals("\uD83D\uDEA9", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns check symbol for 'check'")
    void getBadgeSymbol_shouldReturnCheckSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("check");

        assertTrue(symbol.isPresent());
        assertEquals("\u2705", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns warning symbol for 'warning'")
    void getBadgeSymbol_shouldReturnWarningSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("warning");

        assertTrue(symbol.isPresent());
        assertEquals("\u26A0\uFE0F", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns book symbol for 'book'")
    void getBadgeSymbol_shouldReturnBookSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("book");

        assertTrue(symbol.isPresent());
        assertEquals("\uD83D\uDCD6", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns person symbol for 'person'")
    void getBadgeSymbol_shouldReturnPersonSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("person");

        assertTrue(symbol.isPresent());
        assertEquals("\uD83D\uDC64", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns idea symbol for 'idea'")
    void getBadgeSymbol_shouldReturnIdeaSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("idea");

        assertTrue(symbol.isPresent());
        assertEquals("\uD83D\uDCA1", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns heart symbol for 'heart'")
    void getBadgeSymbol_shouldReturnHeartSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("heart");

        assertTrue(symbol.isPresent());
        assertEquals("\u2764\uFE0F", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns pin symbol for 'pin'")
    void getBadgeSymbol_shouldReturnPinSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("pin");

        assertTrue(symbol.isPresent());
        assertEquals("\uD83D\uDCCC", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns fire symbol for 'fire'")
    void getBadgeSymbol_shouldReturnFireSymbol() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("fire");

        assertTrue(symbol.isPresent());
        assertEquals("\uD83D\uDD25", symbol.get());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns empty for unknown badge name")
    void getBadgeSymbol_shouldReturnEmptyForUnknown() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("nonexistent");

        assertFalse(symbol.isPresent());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns empty for null badge name")
    void getBadgeSymbol_shouldReturnEmptyForNull() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol(null);

        assertFalse(symbol.isPresent());
    }

    @Test
    @DisplayName("getBadgeSymbol() returns empty for empty string")
    void getBadgeSymbol_shouldReturnEmptyForEmptyString() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("");

        assertFalse(symbol.isPresent());
    }

    @Test
    @DisplayName("getAllBadgeNames() returns all built-in badge names")
    void getAllBadgeNames_shouldReturnAllBuiltInNames() {
        List<String> names = BadgeRegistry.getAllBadgeNames();

        assertEquals(10, names.size());
        assertTrue(names.contains("star"));
        assertTrue(names.contains("flag"));
        assertTrue(names.contains("check"));
        assertTrue(names.contains("warning"));
        assertTrue(names.contains("book"));
        assertTrue(names.contains("person"));
        assertTrue(names.contains("idea"));
        assertTrue(names.contains("heart"));
        assertTrue(names.contains("pin"));
        assertTrue(names.contains("fire"));
    }

    @Test
    @DisplayName("getAllBadgeNames() returns an unmodifiable list")
    void getAllBadgeNames_shouldReturnUnmodifiableList() {
        List<String> names = BadgeRegistry.getAllBadgeNames();

        try {
            names.add("custom");
            // Should not reach here
            assertFalse(true, "Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("getBadgeSymbol() is case-sensitive")
    void getBadgeSymbol_shouldBeCaseSensitive() {
        Optional<String> symbol = BadgeRegistry.getBadgeSymbol("Star");

        assertFalse(symbol.isPresent());
    }
}
