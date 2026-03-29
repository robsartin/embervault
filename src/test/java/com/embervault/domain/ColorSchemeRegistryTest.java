package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ColorSchemeRegistry.
 */
class ColorSchemeRegistryTest {

    @Test
    @DisplayName("getDefault returns Standard scheme")
    void getDefault_returnsStandard() {
        ColorScheme def = ColorSchemeRegistry.getDefault();
        assertNotNull(def);
        assertEquals("Standard", def.name());
    }

    @Test
    @DisplayName("getAllSchemes returns exactly 5 presets")
    void getAllSchemes_returnsFivePresets() {
        List<ColorScheme> schemes = ColorSchemeRegistry.getAllSchemes();
        assertEquals(5, schemes.size());
    }

    @Test
    @DisplayName("getAllSchemes contains Standard, Dark, Solarized Light, Solarized Dark, High Contrast")
    void getAllSchemes_containsExpectedNames() {
        List<String> names = ColorSchemeRegistry.getAllSchemes().stream()
                .map(ColorScheme::name)
                .toList();

        assertTrue(names.contains("Standard"));
        assertTrue(names.contains("Dark"));
        assertTrue(names.contains("Solarized Light"));
        assertTrue(names.contains("Solarized Dark"));
        assertTrue(names.contains("High Contrast"));
    }

    @Test
    @DisplayName("getScheme returns matching scheme by name")
    void getScheme_returnsMatchingScheme() {
        Optional<ColorScheme> dark = ColorSchemeRegistry.getScheme("Dark");
        assertTrue(dark.isPresent());
        assertEquals("Dark", dark.get().name());
    }

    @Test
    @DisplayName("getScheme is case-sensitive")
    void getScheme_isCaseSensitive() {
        Optional<ColorScheme> result = ColorSchemeRegistry.getScheme("dark");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getScheme returns empty for unknown name")
    void getScheme_returnsEmptyForUnknown() {
        Optional<ColorScheme> result = ColorSchemeRegistry.getScheme("Neon");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getScheme rejects null")
    void getScheme_rejectsNull() {
        assertThrows(NullPointerException.class,
                () -> ColorSchemeRegistry.getScheme(null));
    }

    @Test
    @DisplayName("all schemes have non-null color values")
    void allSchemes_haveNonNullColors() {
        for (ColorScheme scheme : ColorSchemeRegistry.getAllSchemes()) {
            assertNotNull(scheme.name(), "name");
            assertNotNull(scheme.canvasBackground(), "canvasBackground");
            assertNotNull(scheme.panelBackground(), "panelBackground");
            assertNotNull(scheme.textColor(), "textColor");
            assertNotNull(scheme.secondaryTextColor(), "secondaryTextColor");
            assertNotNull(scheme.borderColor(), "borderColor");
            assertNotNull(scheme.selectionColor(), "selectionColor");
            assertNotNull(scheme.toolbarBackground(), "toolbarBackground");
            assertNotNull(scheme.accentColor(), "accentColor");
        }
    }

    @Test
    @DisplayName("getAllSchemes returns unmodifiable list")
    void getAllSchemes_returnsUnmodifiableList() {
        List<ColorScheme> schemes = ColorSchemeRegistry.getAllSchemes();
        assertThrows(UnsupportedOperationException.class,
                () -> schemes.add(ColorSchemeRegistry.getDefault()));
    }
}
