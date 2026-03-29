package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ColorScheme record.
 */
class ColorSchemeTest {

    @Test
    @DisplayName("ColorScheme stores all color properties")
    void colorScheme_storesAllProperties() {
        ColorScheme scheme = new ColorScheme(
                "Test", "#F0F0F0", "#FFFFFF", "#000000",
                "#666666", "#CCCCCC", "#3399FF",
                "#E0E0E0", "#0066CC");

        assertEquals("Test", scheme.name());
        assertEquals("#F0F0F0", scheme.canvasBackground());
        assertEquals("#FFFFFF", scheme.panelBackground());
        assertEquals("#000000", scheme.textColor());
        assertEquals("#666666", scheme.secondaryTextColor());
        assertEquals("#CCCCCC", scheme.borderColor());
        assertEquals("#3399FF", scheme.selectionColor());
        assertEquals("#E0E0E0", scheme.toolbarBackground());
        assertEquals("#0066CC", scheme.accentColor());
    }

    @Test
    @DisplayName("ColorScheme record equality based on all fields")
    void colorScheme_equalityBasedOnAllFields() {
        ColorScheme a = new ColorScheme(
                "A", "#F0F0F0", "#FFFFFF", "#000000",
                "#666666", "#CCCCCC", "#3399FF",
                "#E0E0E0", "#0066CC");
        ColorScheme b = new ColorScheme(
                "A", "#F0F0F0", "#FFFFFF", "#000000",
                "#666666", "#CCCCCC", "#3399FF",
                "#E0E0E0", "#0066CC");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("ColorScheme toString includes name")
    void colorScheme_toStringIncludesName() {
        ColorScheme scheme = new ColorScheme(
                "Dark", "#1E1E1E", "#2D2D2D", "#D4D4D4",
                "#808080", "#404040", "#264F78",
                "#333333", "#569CD6");

        assertNotNull(scheme.toString());
        assertEquals(true, scheme.toString().contains("Dark"));
    }
}
